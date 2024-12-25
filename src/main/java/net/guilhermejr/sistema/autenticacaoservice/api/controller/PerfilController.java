package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.response.PerfilResponse;
import net.guilhermejr.sistema.autenticacaoservice.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/perfis")
public class PerfilController {

    private final PerfilService perfilService;

    // --- Retornar -----------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<PerfilResponse>> retornar() {

        log.info("Retornando perfis");
        List<PerfilResponse> perfilResponses = perfilService.retornar();
        return ResponseEntity.status(HttpStatus.OK).body(perfilResponses);

    }

}
