package com.acougue.modules.pdv;

import com.acougue.entity.Modulo;
import com.acougue.modules.pdv.PixService.PixChargeResponse;
import com.acougue.security.Acao;
import com.acougue.security.ExigirPermissao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/pagamento")
@RequiredArgsConstructor
public class PagamentoController {

    private final PixService pixService;

    @ExigirPermissao(modulo = Modulo.PDV, acao = Acao.CRIAR)
    @PostMapping("/pix/criar")
    public ResponseEntity<PixChargeResponse> criarPix(
            @RequestBody Map<String, Object> body) {

        BigDecimal valor  = new BigDecimal(body.get("valor").toString());
        Long       vendaId = body.get("vendaId") != null
                ? Long.parseLong(body.get("vendaId").toString())
                : null;

        return ResponseEntity.ok(pixService.criarCobranca(valor, vendaId));
    }

    @ExigirPermissao(modulo = Modulo.PDV, acao = Acao.VER)
    @GetMapping("/pix/status/{mpPaymentId}")
    public ResponseEntity<Map<String, String>> statusPix(
            @PathVariable String mpPaymentId) {
        return ResponseEntity.ok(pixService.verificarStatus(mpPaymentId));
    }

    /*
    !!NÃO anotar com @ExigirPermissao — chamado pelo Mercado Pago sem usuário
    !!logado (já é permitAll() no SecurityConfig). Colocar a trava aqui quebraria
    !!o webhook, pois não existe um UsuarioAutenticado nessa requisição.
    */
    @PostMapping("/pix/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        pixService.processarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}