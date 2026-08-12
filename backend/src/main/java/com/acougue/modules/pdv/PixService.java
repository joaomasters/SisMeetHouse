package com.acougue.modules.pdv;

import com.acougue.entity.PagamentoPix;
import com.acougue.exception.BusinessException;
import com.acougue.repository.PagamentoPixRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PixService {

    @Value("${pagamento.pix.mp-access-token:}")
    private String mpAccessToken;

    private final PagamentoPixRepository pixRepo;
    private final ObjectMapper objectMapper;

    // ── Cria cobrança PIX via Mercado Pago ──────────────────────────────────
    public PixChargeResponse criarCobranca(BigDecimal valor, Long vendaId) {
        if (mpAccessToken == null || mpAccessToken.isBlank()) {
            throw new BusinessException(
                "Integração PIX não configurada. " +
                "Adicione a variável de ambiente MP_ACCESS_TOKEN no Railway.");
        }

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                "transaction_amount", valor,
                "payment_method_id",  "pix",
                "payer", Map.of("email", "comprador@acougue.local"),
                "description", "Venda AcougueERP" + (vendaId != null ? " #" + vendaId : "")
            ));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mercadopago.com/v1/payments"))
                .header("Authorization",      "Bearer " + mpAccessToken)
                .header("Content-Type",       "application/json")
                .header("X-Idempotency-Key",  UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 201) {
                throw new BusinessException("MP retornou " + resp.statusCode() + ": " + resp.body());
            }

            JsonNode json    = objectMapper.readTree(resp.body());
            Long   mpId      = json.get("id").asLong();
            String qrCode    = json.at("/point_of_interaction/transaction_data/qr_code").asText("");
            String qrBase64  = json.at("/point_of_interaction/transaction_data/qr_code_base64").asText("");

            PagamentoPix pix = PagamentoPix.builder()
                .vendaId(vendaId)
                .mpPaymentId(mpId)
                .valor(valor)
                .qrCode(qrCode)
                .qrCodeBase64(qrBase64)
                .status("PENDENTE")
                .build();
            pixRepo.save(pix);

            return new PixChargeResponse(pix.getId(), mpId.toString(), qrCode, qrBase64, "PENDENTE");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erro ao criar cobrança PIX: " + e.getMessage());
        }
    }

    // ── Verifica status no MP ────────────────────────────────────────────────
    public Map<String, String> verificarStatus(String mpPaymentId) {
        if (mpAccessToken == null || mpAccessToken.isBlank()) {
            return Map.of("status", "ERRO", "message", "PIX não configurado");
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mercadopago.com/v1/payments/" + mpPaymentId))
                .header("Authorization", "Bearer " + mpAccessToken)
                .GET()
                .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(resp.body());
            String status = json.has("status") ? json.get("status").asText() : "error";

            String normalizado = switch (status) {
                case "approved"                  -> "APROVADO";
                case "pending", "in_process"     -> "PENDENTE";
                case "cancelled", "refunded",
                     "charged_back", "rejected"  -> "EXPIRADO";
                default                          -> "ERRO";
            };

            // Atualiza no banco quando aprovado
            if ("APROVADO".equals(normalizado)) {
                try {
                    pixRepo.findByMpPaymentId(Long.parseLong(mpPaymentId)).ifPresent(p -> {
                        if (!"APROVADO".equals(p.getStatus())) {
                            p.setStatus("APROVADO");
                            p.setConfirmedAt(LocalDateTime.now());
                            pixRepo.save(p);
                        }
                    });
                } catch (NumberFormatException ignored) {}
            }

            return Map.of("status", normalizado);

        } catch (Exception e) {
            return Map.of("status", "ERRO", "message", e.getMessage());
        }
    }

    // ── Webhook do Mercado Pago ──────────────────────────────────────────────
    public void processarWebhook(Map<String, Object> payload) {
        try {
            Object type = payload.get("type");
            if (!"payment".equals(type)) return;

            Object data = payload.get("data");
            if (!(data instanceof Map<?, ?> dataMap)) return;

            Object idObj = dataMap.get("id");
            if (idObj == null) return;

            String mpIdStr = idObj.toString();
            verificarStatus(mpIdStr);   // atualiza o banco via polling normal

        } catch (Exception ignored) {}
    }

    // ── DTO de resposta ──────────────────────────────────────────────────────
    public record PixChargeResponse(
        Long   id,
        String mpPaymentId,
        String qrCode,
        String qrCodeBase64,
        String status
    ) {}
}
