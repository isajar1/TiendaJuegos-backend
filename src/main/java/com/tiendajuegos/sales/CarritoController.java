package com.tiendajuegos.sales;

import com.tiendajuegos.dto.ApiDtos.CarritoRequest;
import com.tiendajuegos.dto.ApiDtos.CarritoResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping
    public CarritoResponse calcular(@RequestBody CarritoRequest request) {
        return carritoService.calcular(request);
    }
}
