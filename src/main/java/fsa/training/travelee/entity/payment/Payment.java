package fsa.training.travelee.entity.payment;

import fsa.training.travelee.entity.booking.Booking;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String paymentCode;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // MOMO, ZALOPAY, BANK_TRANSFER, CREDIT_CARD, QR_CODE

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, COMPLETED, FAILED, REFUNDED

    private String transactionId;
    private String gatewayResponse;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    // Thông tin hoàn tiền (nếu có)
    @Column(precision = 18, scale = 2)
    private BigDecimal refundAmount;

    @Column(columnDefinition = "TEXT")
    private String refundReason;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;
}