package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.request.TrocaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.UsuarioAtualizarRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.UsuarioRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.UsuarioResponse;
import net.guilhermejr.sistema.autenticacaoservice.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // --- Retornar -----------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> retornar(@PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable paginacao) {

        log.info("Retornando usuários");
        Page<UsuarioResponse> usuarioResponses = usuarioService.retornar(paginacao);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponses);

    }

    // --- Atualizar ----------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioAtualizarRequest usuarioAtualizarRequest) {

        log.info("Atualizando usuário: {}", id);
        UsuarioResponse usuarioResponse = usuarioService.atualizar(id, usuarioAtualizarRequest);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponse);

    }

    // --- RetornarUm ---------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> retornarUm(@PathVariable UUID id) {

        log.info("Recuperando um usuário: {}", id);
        UsuarioResponse usuarioResponse = usuarioService.retornarUm(id);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponse);

    }

    // --- TrocarSenha --------------------------------------------------------
    @PutMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody TrocaSenhaRequest trocaSenhaRequest) {

        log.info("Trocando senha do usuário");
        usuarioService.trocarSenha(trocaSenhaRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    // --- Inserir ------------------------------------------------------------
    @PostMapping
    public ResponseEntity<UsuarioResponse> inserir(@Valid @RequestBody UsuarioRequest usuarioRequest) {

        log.info("Inserindo usuário: {}", usuarioRequest.getEmail());
        UsuarioResponse usuarioResponse = usuarioService.inserir(usuarioRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioResponse);

    }

    // --- AlterarStatus ------------------------------------------------------
    @PutMapping(path = "{id}/alterar-status")
    public ResponseEntity<Void> alterarStatus(@PathVariable UUID id) {

        usuarioService.alterarStatus(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
