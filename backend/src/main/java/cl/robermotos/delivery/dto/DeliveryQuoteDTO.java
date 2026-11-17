package cl.robermotos.delivery.dto;
public record DeliveryQuoteDTO(String provider,String displayName,long priceClp,int estimatedMinutes,double distanceKm,String quoteId) {}