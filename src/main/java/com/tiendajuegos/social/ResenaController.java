package com.tiendajuegos.social;

import com.tiendajuegos.dto.ApiDtos.ResenaRequest;
import com.tiendajuegos.dto.ApiDtos.ResenaResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/juegos/{juegoId}/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping
    public List<ResenaResponse> listar(@PathVariable Long juegoId) {
        return resenaService.listar(juegoId);
    }

    @PostMapping
    public ResponseEntity<ResenaResponse> crear(@PathVariable Long juegoId,
                                                 @RequestBody ResenaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.crear(juegoId, request));
    }
}
