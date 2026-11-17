package cl.robermotos.payment;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/payments/webpay")
public class WebpayController {
    private final WebpayService service;
    private final String frontendUrl;

    public WebpayController(
        WebpayService service,
        @Value("${frontend-url}") String frontendUrl
    ) {
        this.service = service;
        this.frontendUrl = frontendUrl;
    }

    @PostMapping("/create")
    public Map<String, String> create(@RequestBody CreatePaymentRequest request) {
        return service.create(
            request.orderId(),
            request.amount(),
            request.provider(),
            request.quoteId(),
            request.delivery()
        );
    }

    @RequestMapping(value = "/return", method = {RequestMethod.GET, RequestMethod.POST})
    public RedirectView returned(
        @RequestParam(value = "token_ws", required = false) String token,
        @RequestParam(value = "TBK_TOKEN", required = false) String tbkToken
    ) {
        if (token == null) {
            return redirectToFrontend("cancelled", null);
        }

        Map<String, String> result = service.commit(token);
        return redirectToFrontend(result.get("status"), result.get("orderId"));
    }

    private RedirectView redirectToFrontend(String paymentStatus, String orderId) {
        UriComponentsBuilder redirect = UriComponentsBuilder.fromUriString(frontendUrl)
            .replacePath("/")
            .replaceQuery(null)
            .queryParam("payment", paymentStatus);
        if (orderId != null) {
            redirect.queryParam("orderId", orderId);
        }
        return new RedirectView(redirect.build().toUriString());
    }
}

