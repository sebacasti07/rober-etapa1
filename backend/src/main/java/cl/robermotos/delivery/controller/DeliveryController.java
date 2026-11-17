package cl.robermotos.delivery.controller;

import cl.robermotos.delivery.dto.DeliveryQuoteDTO;
import cl.robermotos.delivery.dto.DeliveryRequest;
import cl.robermotos.delivery.dto.DispatchResult;
import cl.robermotos.delivery.service.DeliveryOrchestratorService;
import cl.robermotos.delivery.service.TrackingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DeliveryController {
    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

    private final DeliveryOrchestratorService orchestrator;
    private final TrackingService tracking;

    public DeliveryController(DeliveryOrchestratorService orchestrator, TrackingService tracking) {
        this.orchestrator = orchestrator;
        this.tracking = tracking;
    }

    @GetMapping("/test")
    public String test() {
        return "OK";
    }

    @GetMapping("/debug-quotes")
    public List<DeliveryQuoteDTO> debugQuotes() {
        DeliveryRequest request = new DeliveryRequest(
            -33.3650,
            -70.7290,
            "Av. Bernardo O'Higgins 100, Quilicura, Santiago, Chile",
            -33.4489,
            -70.6693,
            "Plaza de Armas, Santiago, Chile"
        );
        return orchestrator.quotes(request);
    }

    @PostMapping("/deliveries/quotes")
    public List<DeliveryQuoteDTO> quotes(@Valid @RequestBody DeliveryRequest request) {
        log.info("Delivery quote request received: {}", request);
        try {
            List<DeliveryQuoteDTO> quotes = orchestrator.quotes(request);
            log.info("Delivery quote count: {}", quotes.size());
            log.info("Delivery quote result list: {}", quotes);
            return quotes;
        } catch (RuntimeException exception) {
            log.error("Failed to generate delivery quotes", exception);
            throw exception;
        }
    }

    @PostMapping("/deliveries/{orderId}/dispatch")
    public DispatchResult dispatch(
        @PathVariable String orderId,
        @RequestParam String provider,
        @RequestParam String quoteId,
        @RequestBody DeliveryRequest request
    ) {
        DispatchResult result = orchestrator.dispatch(orderId, provider, quoteId, request);
        tracking.start(orderId);
        return result;
    }

    @GetMapping("/deliveries/{orderId}/status")
    public Map<String, String> status(@PathVariable String orderId) {
        return Map.of(
            "orderId", orderId,
            "status", tracking.get(orderId).name()
        );
    }
}
