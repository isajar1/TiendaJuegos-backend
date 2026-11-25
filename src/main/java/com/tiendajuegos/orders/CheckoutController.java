package com.tiendajuegos.orders;

import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.orders.OrderDtos.OrdenResponse;
import com.tiendajuegos.orders.OrderDtos.PagoRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CurrentUserService currentUserService;

    public CheckoutController(CheckoutService checkoutService, CurrentUserService currentUserService) {
        this.checkoutService = checkoutService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/pagar")
    public OrdenResponse pagar(@RequestBody(required = false) PagoRequest request,
                               Authentication authentication) {
        boolean aprobar = request == null || request.aprobar() == null || request.aprobar();
        return checkoutService.pagar(currentUserService.id(authentication), aprobar);
    }
}
