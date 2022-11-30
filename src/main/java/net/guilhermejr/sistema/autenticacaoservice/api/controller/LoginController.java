package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.request.EsqueciMinhaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.LoginRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.JWTResponde;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionDefault;
import net.guilhermejr.sistema.autenticacaoservice.exception.dto.ErrorDefaultDTO;
import net.guilhermejr.sistema.autenticacaoservice.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Log4j2
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping
public class LoginController {

    private final LoginService loginService;

    // --- Login --------------------------------------------------------------
    @Operation(summary = "Login do usuário", responses = {
            @ApiResponse(responseCode = "200", description = "OK",content = @Content(mediaType = "application/json", schema = @Schema(implementation = JWTResponde.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JWTResponde> login(@Valid @RequestBody LoginRequest loginRequest) {

        log.info("Iniciando login do usuário");
        JWTResponde jwtResponde = loginService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(jwtResponde);

    }


    // --- EsqueciMinhaSenha --------------------------------------------------
    @Operation(summary = "Envia nova senha para o usuário", responses = {
            @ApiResponse(responseCode = "204", description = "E-mail enviado com nova senha"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @PostMapping(path = "/esqueci-minha-senha")
    public ResponseEntity<Void> esqueciMinhaSenha(@Valid @RequestBody EsqueciMinhaSenhaRequest esqueciMinhaSenhaRequest) {

        loginService.esqueciMinhaSenha(esqueciMinhaSenhaRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
