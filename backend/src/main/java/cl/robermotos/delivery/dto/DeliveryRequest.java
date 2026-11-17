package cl.robermotos.delivery.dto;
import jakarta.validation.constraints.*;
public record DeliveryRequest(@NotNull Double pickupLat,@NotNull Double pickupLng,@NotBlank String pickupAddress,@NotNull Double dropoffLat,@NotNull Double dropoffLng,@NotBlank String dropoffAddress) {}