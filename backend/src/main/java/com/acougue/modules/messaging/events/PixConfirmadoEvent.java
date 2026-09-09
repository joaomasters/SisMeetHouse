package com.acougue.modules.messaging.events;

import java.math.BigDecimal;

public record PixConfirmadoEvent(
        Long       vendaId,
        Long       mpPaymentId,
        BigDecimal valor
) {}
