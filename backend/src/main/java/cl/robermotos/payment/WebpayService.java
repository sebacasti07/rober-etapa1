package cl.robermotos.payment;

import cl.robermotos.config.DeliveryProperties;
import cl.robermotos.delivery.dto.DeliveryRequest;
import cl.robermotos.delivery.service.DeliveryOrchestratorService;
import cl.robermotos.order.OrderStore;
import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.common.IntegrationCommerceCodes;
import cl.transbank.common.IntegrationType;
import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WebpayService {
    private final DeliveryOrchestratorService delivery;
    private final OrderStore orders;

    public WebpayService(DeliveryOrchestratorService delivery, OrderStore orders, DeliveryProperties properties) {
        this.delivery = delivery;
        this.orders = orders;
    }

    private WebpayPlus.Transaction transaction() {
        return new WebpayPlus.Transaction(
            new WebpayOptions(
                IntegrationCommerceCodes.WEBPAY_PLUS,
                IntegrationApiKeys.WEBPAY,
                IntegrationType.TEST
            )
        );
    }

    public Map<String, String> create(
        String orderId,
        long amount,
        String provider,
        String quoteId,
        DeliveryRequest request
    ) {
        try {
            orders.save(orderId, provider, quoteId, request);
            var response = transaction().create(
                orderId,
                UUID.randomUUID().toString(),
                amount,
                "http://localhost:8080/api/v1/payments/webpay/return"
            );
            orders.token(response.getToken(), orderId);

            return Map.of(
                "url", response.getUrl(),
                "token", response.getToken()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo crear transacción Webpay", exception);
        }
    }

    public Map<String, String> commit(String token) {
        try {
            var response = transaction().commit(token);
            String orderId = orders.byToken(token);

            if (orderId == null) {
                throw new IllegalArgumentException("Token Webpay desconocido");
            }

            if ("AUTHORIZED".equals(response.getStatus())
                && response.getResponseCode() == 0
                && !orders.paid(orderId)) {
                orders.markPaid(orderId);
                var order = orders.get(orderId);
                delivery.dispatch(orderId, order.provider, order.quoteId, order.request);
            }

            return Map.of(
                "status", response.getStatus(),
                "responseCode", String.valueOf(response.getResponseCode()),
                "orderId", orderId
            );
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo confirmar Webpay", exception);
        }
    }
}

