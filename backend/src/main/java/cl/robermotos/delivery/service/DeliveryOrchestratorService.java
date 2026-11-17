package cl.robermotos.delivery.service;

import cl.robermotos.config.DeliveryProperties;
import cl.robermotos.delivery.adapter.DeliveryProviderAdapter;
import cl.robermotos.delivery.dto.DeliveryQuoteDTO;
import cl.robermotos.delivery.dto.DeliveryRequest;
import cl.robermotos.delivery.dto.DispatchResult;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeliveryOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(DeliveryOrchestratorService.class);

    private final List<DeliveryProviderAdapter> adapters;
    private final DeliveryProperties props;
    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private final Map<String, DeliveryQuoteDTO> quotes = new ConcurrentHashMap<>();
    private final Map<String, Long> quoteTimes = new ConcurrentHashMap<>();

    public DeliveryOrchestratorService(List<DeliveryProviderAdapter> adapters, DeliveryProperties props) {
        this.adapters = adapters;
        this.props = props;
    }

    public List<DeliveryQuoteDTO> quotes(DeliveryRequest request) {
        List<CompletableFuture<DeliveryQuoteDTO>> futures = adapters.stream()
            .map(adapter -> CompletableFuture
                .supplyAsync(() -> adapter.quote(request), pool)
                .orTimeout(props.quoteTimeoutMs, TimeUnit.MILLISECONDS)
                .exceptionally(exception -> {
                    log.error("Quote generation failed for provider {}", adapter.provider(), exception);
                    return null;
                }))
            .toList();

        List<DeliveryQuoteDTO> results = futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .toList();

        results.forEach(quote -> {
            quotes.put(quote.quoteId(), quote);
            quoteTimes.put(quote.quoteId(), System.currentTimeMillis());
        });

        return results.stream()
            .sorted(Comparator.comparingLong(DeliveryQuoteDTO::priceClp))
            .toList();
    }

    public DispatchResult dispatch(String orderId, String provider, String quoteId, DeliveryRequest request) {
        DeliveryProviderAdapter selected = find(provider);
        DeliveryQuoteDTO quote = quotes.get(quoteId);
        if (quote == null
            || !quote.provider().equals(provider)
            || System.currentTimeMillis() - quoteTimes.getOrDefault(quoteId, 0L) > props.quoteTtlSeconds * 1000) {
            quote = selected.quote(request);
            quotes.put(quote.quoteId(), quote);
            quoteTimes.put(quote.quoteId(), System.currentTimeMillis());
            quoteId = quote.quoteId();
        }

        if (props.simulateNoRiders) {
            System.err.println("ALERTA: " + provider + " sin repartidores. Activando failover.");
            return adapters.stream()
                .filter(adapter -> !adapter.provider().equals(provider))
                .map(adapter -> Map.entry(adapter, adapter.quote(request)))
                .sorted(Comparator.comparingLong(entry -> entry.getValue().priceClp()))
                .map(entry -> {
                    System.err.println("FAILOVER: reasignando a " + entry.getKey().displayName());
                    return entry.getKey().dispatch(orderId, request, entry.getValue().quoteId());
                })
                .findFirst()
                .orElseThrow();
        }

        return selected.dispatch(orderId, request, quote.quoteId());
    }

    private DeliveryProviderAdapter find(String name) {
        return adapters.stream()
            .filter(adapter -> adapter.provider().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Proveedor desconocido: " + name));
    }
}
