package cl.robermotos.delivery.controller;

import cl.robermotos.delivery.dto.DeliveryQuoteDTO;
import cl.robermotos.delivery.dto.DeliveryRequest;
import cl.robermotos.delivery.dto.DispatchResult;
import cl.robermotos.delivery.service.DeliveryOrchestratorService;
import cl.robermotos.delivery.service.TrackingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryController {
    private final DeliveryOrchestratorService orchestrator;
    private final TrackingService tracking;

    public DeliveryController(DeliveryOrchestratorService orchestrator, TrackingService tracking) {
        this.orchestrator = orchestrator;
        this.tracking = tracking;
    }

    @PostMapping("/deliveries/quotes")
    public List<DeliveryQuoteDTO> quotes(@Valid @RequestBody DeliveryRequest request) {
        return orchestrator.quotes(request);
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
