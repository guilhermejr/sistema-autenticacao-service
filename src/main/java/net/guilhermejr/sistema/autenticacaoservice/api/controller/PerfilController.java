package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.response.PerfilResponse;
import net.guilhermejr.sistema.autenticacaoservice.exception.dto.ErrorDefaultDTO;
import net.guilhermejr.sistema.autenticacaoservice.service.PerfilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/perfis")
public class PerfilController {

    private final PerfilService perfilService;

    // --- Retornar -----------------------------------------------------------
    @Operation(summary = "Retorna perfis", responses = {
            @ApiResponse(responseCode = "200", description = "OK",content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PerfilResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<PerfilResponse>> retornar() {

        log.info("Retornando perfis");
        List<PerfilResponse> perfilResponses = perfilService.retornar();
        return ResponseEntity.status(HttpStatus.OK).body(perfilResponses);

    }

}
