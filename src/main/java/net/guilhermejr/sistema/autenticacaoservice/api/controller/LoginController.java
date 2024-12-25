package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.request.EsqueciMinhaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.LoginRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.JWTResponde;
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
    public ResponseEntity<JWTResponde> login(@Valid @RequestBody LoginRequest loginRequest) {

        log.info("Iniciando login do usuário");
        JWTResponde jwtResponde = loginService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(jwtResponde);

    }


    // --- EsqueciMinhaSenha --------------------------------------------------
    @PostMapping(path = "/esqueci-minha-senha")
    public ResponseEntity<Void> esqueciMinhaSenha(@Valid @RequestBody EsqueciMinhaSenhaRequest esqueciMinhaSenhaRequest) {

        loginService.esqueciMinhaSenha(esqueciMinhaSenhaRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
