package com.acougue.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento_pix")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PagamentoPix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venda_id")
    private Long vendaId;

    @Column(name = "mp_payment_id")
    private Long mpPaymentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDENTE";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
}
