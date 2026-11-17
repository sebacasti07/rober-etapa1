package cl.robermotos.delivery.adapter;

import cl.robermotos.config.DeliveryProperties;
import cl.robermotos.delivery.dto.DeliveryQuoteDTO;
import cl.robermotos.delivery.dto.DeliveryRequest;
import cl.robermotos.delivery.dto.DispatchResult;
import cl.robermotos.delivery.service.Geo;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UberDirectAdapter extends AbstractMockAdapter {
    private final DeliveryProperties props;
    private final WebClient http = WebClient.builder().build();

    public UberDirectAdapter(DeliveryProperties props) {
        this.props = props;
    }

    @Override
    public String provider() {
        return "UBER_DIRECT";
    }

    @Override
    public String displayName() {
        return "Uber Direct";
    }

    @Override
    protected long base() {
        return 2500;
    }

    @Override
    protected long perKm() {
        return 420;
    }

    @Override
    protected int etaBase() {
        return 30;
    }

    @Override
    public DeliveryQuoteDTO quote(DeliveryRequest request) {
        if (!props.getUber().enabled || props.getUber().clientId == null || props.getUber().clientId.isBlank()) {
            return super.quote(request);
        }

        try {
            String token = token();
            Map<String, Object> body = Map.of(
                "pickup_address", addressJson(request.pickupAddress()),
                "dropoff_address", addressJson(request.dropoffAddress()),
                "pickup_latitude", request.pickupLat(),
                "pickup_longitude", request.pickupLng(),
                "dropoff_latitude", request.dropoffLat(),
                "dropoff_longitude", request.dropoffLng()
            );

            Map<String, Object> result = http.post()
                .uri(props.getUber().baseUrl + "/v1/customers/" + props.getUber().customerId + "/delivery_quotes")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            double km = Geo.haversine(request.pickupLat(), request.pickupLng(), request.dropoffLat(), request.dropoffLng());
            Object fee = result == null ? 2500 : result.getOrDefault("fee", 2500);
            long price = Long.parseLong(String.valueOf(fee));

            return new DeliveryQuoteDTO(
                provider(),
                displayName(),
                Geo.ceilTen(price),
                40,
                km,
                String.valueOf(result == null ? 0 : result.get("id"))
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Uber Direct quote failed: " + exception.getMessage(), exception);
        }
    }

    private String token() {
        Map<String, Object> response = http.post()
            .uri(props.getUber().authUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue(
                "client_id=" + props.getUber().clientId
                    + "&client_secret=" + props.getUber().clientSecret
                    + "&grant_type=client_credentials&scope=eats.deliveries"
            )
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        return String.valueOf(response.get("access_token"));
    }

    @Override
    public DispatchResult dispatch(String orderId, DeliveryRequest request, String quoteId) {
        if (!props.getUber().enabled || props.getUber().clientId == null || props.getUber().clientId.isBlank()) {
            return super.dispatch(orderId, request, quoteId);
        }

        try {
            String token = token();
            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("quote_id", quoteId);
            body.put("pickup_address", addressJson(request.pickupAddress()));
            body.put("pickup_name", props.getPickup().name);
            body.put("pickup_phone_number", props.getPickup().phone);
            body.put("pickup_latitude", request.pickupLat());
            body.put("pickup_longitude", request.pickupLng());
            body.put("dropoff_address", addressJson(request.dropoffAddress()));
            body.put("dropoff_name", "Cliente");
            body.put("dropoff_phone_number", "+56911111111");
            body.put("dropoff_latitude", request.dropoffLat());
            body.put("dropoff_longitude", request.dropoffLng());

            Map<String, Object> result = http.post()
                .uri(props.getUber().baseUrl + "/v1/customers/" + props.getUber().customerId + "/deliveries")
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return new DispatchResult(provider(), String.valueOf(result.get("id")), orderId, "BUSCANDO_REPARTIDOR");
        } catch (Exception exception) {
            throw new IllegalStateException("Uber Direct dispatch failed: " + exception.getMessage(), exception);
        }
    }

    private String addressJson(String address) {
        String sanitized = address.replace("\"", "\\\"");
        return "{\"street_address\":[\"" + sanitized + "\"],\"city\":\"Santiago\",\"country\":\"CL\"}";
    }
}
