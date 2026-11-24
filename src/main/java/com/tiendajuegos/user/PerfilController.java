package com.tiendajuegos.user;

import com.tiendajuegos.dto.ApiDtos.PerfilRequest;
import com.tiendajuegos.dto.ApiDtos.PerfilResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/me")
    public PerfilResponse yo() {
        return perfilService.yo();
    }

    @PutMapping("/me")
    public PerfilResponse actualizar(@RequestBody PerfilRequest request) {
        return perfilService.actualizar(request);
    }
}
