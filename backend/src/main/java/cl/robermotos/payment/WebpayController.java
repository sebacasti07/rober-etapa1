package cl.robermotos.payment;

import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/payments/webpay")
@CrossOrigin(origins = "http://localhost:5173")
public class WebpayController {
    private final WebpayService service;

    public WebpayController(WebpayService service) {
        this.service = service;
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
            return new RedirectView("http://localhost:5173/?payment=cancelled");
        }

        Map<String, String> result = service.commit(token);
        return new RedirectView(
            "http://localhost:5173/?payment=" + result.get("status") + "&orderId=" + result.get("orderId")
        );
    }
}

