package com.acougue.modules.pdv;

import com.acougue.modules.pdv.PixService.PixChargeResponse;
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

    

    @PostMapping("/pix/criar")
    public ResponseEntity<PixChargeResponse> criarPix(
            @RequestBody Map<String, Object> body) {

        BigDecimal valor  = new BigDecimal(body.get("valor").toString());
        Long       vendaId = body.get("vendaId") != null
            ? Long.parseLong(body.get("vendaId").toString())
            : null;

        return ResponseEntity.ok(pixService.criarCobranca(valor, vendaId));
    }

    

    @GetMapping("/pix/status/{mpPaymentId}")
    public ResponseEntity<Map<String, String>> statusPix(
            @PathVariable String mpPaymentId) {
        return ResponseEntity.ok(pixService.verificarStatus(mpPaymentId));
    }

    

    @PostMapping("/pix/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        pixService.processarWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
