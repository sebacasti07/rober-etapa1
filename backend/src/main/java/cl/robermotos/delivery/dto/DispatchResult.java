package cl.robermotos.delivery.dto;
public record DispatchResult(String provider,String externalDeliveryId,String orderId,String status) {}