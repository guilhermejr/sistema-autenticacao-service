package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.request.EsqueciMinhaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.LoginDoisFatoresRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.LoginRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.RefreshTokenRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.JWTResponde;
import net.guilhermejr.sistema.autenticacaoservice.api.response.LoginResponse;
import net.guilhermejr.sistema.autenticacaoservice.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping
public class LoginController {

    private final LoginService loginService;

    // --- Login --------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        log.info("Iniciando login do usuário");
        LoginResponse loginResponse = loginService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);

    }

    @PostMapping("/login/dois-fatores")
    public ResponseEntity<JWTResponde> loginDoisFatores(@Valid @RequestBody LoginDoisFatoresRequest loginDoisFatoresRequest) {

        log.info("Iniciando login do usuário: código de dois fatores");
        JWTResponde jwtResponde = loginService.loginDoisFatores(loginDoisFatoresRequest);
        return ResponseEntity.status(HttpStatus.OK).body(jwtResponde);

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JWTResponde> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        log.info("Post: {}", refreshTokenRequest);
        log.info("Iniciando refreshtoken");
        JWTResponde jwtResponde = loginService.refreshToken(refreshTokenRequest);
        return ResponseEntity.status(HttpStatus.OK).body(jwtResponde);

    }


    // --- EsqueciMinhaSenha --------------------------------------------------
    @PostMapping(path = "/esqueci-minha-senha")
    public ResponseEntity<Void> esqueciMinhaSenha(@Valid @RequestBody EsqueciMinhaSenhaRequest esqueciMinhaSenhaRequest) {

        loginService.esqueciMinhaSenha(esqueciMinhaSenhaRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
