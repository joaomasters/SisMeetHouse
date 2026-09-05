package com.acougue.modules.messaging.events;

import java.math.BigDecimal;

/**
 * Evento publicado quando um pagamento PIX é confirmado via webhook do Mercado Pago.
 * Permite que o PDV pare o polling e receba a confirmação em tempo real
 * (quando integrado com WebSocket/SSE).
 */
public record PixConfirmadoEvent(
        Long       vendaId,
        Long       mpPaymentId,
        BigDecimal valor
) {}
