package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.request.CodigoDoisFatoresRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.DoisFatoresConfiguracaoResponse;
import net.guilhermejr.sistema.autenticacaoservice.api.response.DoisFatoresStatusResponse;
import net.guilhermejr.sistema.autenticacaoservice.service.DoisFatoresService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// --- Sem @PreAuthorize: qualquer usuário autenticado configura os próprios dois fatores ---
@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/dois-fatores")
public class DoisFatoresController {

    private final DoisFatoresService doisFatoresService;

    // --- Status -------------------------------------------------------------
    @GetMapping
    public ResponseEntity<DoisFatoresStatusResponse> status() {

        return ResponseEntity.status(HttpStatus.OK).body(doisFatoresService.status());

    }

    // --- Configurar ---------------------------------------------------------
    @PostMapping("/configurar")
    public ResponseEntity<DoisFatoresConfiguracaoResponse> configurar() {

        log.info("Configurando dois fatores");
        return ResponseEntity.status(HttpStatus.OK).body(doisFatoresService.configurar());

    }

    // --- Ativar -------------------------------------------------------------
    @PostMapping("/ativar")
    public ResponseEntity<Void> ativar(@Valid @RequestBody CodigoDoisFatoresRequest codigoDoisFatoresRequest) {

        log.info("Ativando dois fatores");
        doisFatoresService.ativar(codigoDoisFatoresRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    // --- Desativar ----------------------------------------------------------
    @PostMapping("/desativar")
    public ResponseEntity<Void> desativar(@Valid @RequestBody CodigoDoisFatoresRequest codigoDoisFatoresRequest) {

        log.info("Desativando dois fatores");
        doisFatoresService.desativar(codigoDoisFatoresRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
