package cl.robermotos.payment;

import cl.robermotos.delivery.dto.DeliveryRequest;

public record CreatePaymentRequest(
    String orderId,
    long amount,
    String provider,
    String quoteId,
    DeliveryRequest delivery
) {
}
