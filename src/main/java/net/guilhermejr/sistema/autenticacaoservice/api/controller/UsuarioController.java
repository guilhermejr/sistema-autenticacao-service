package net.guilhermejr.sistema.autenticacaoservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.request.TrocaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.UsuarioAtualizarRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.UsuarioRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.UsuarioResponse;
import net.guilhermejr.sistema.autenticacaoservice.exception.dto.ErrorDefaultDTO;
import net.guilhermejr.sistema.autenticacaoservice.exception.dto.ErrorRequestDTO;
import net.guilhermejr.sistema.autenticacaoservice.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // --- Retornar -----------------------------------------------------------
    @Operation(summary = "Retorna usuários", responses = {
            @ApiResponse(responseCode = "200", description = "OK",content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> retornar(@PageableDefault(page = 0, size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable paginacao) {

        log.info("Retornando usuários");
        Page<UsuarioResponse> usuarioResponses = usuarioService.retornar(paginacao);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponses);

    }

    // --- Atualizar ----------------------------------------------------------
    @Operation(summary = "Atualiza um usuário", responses = {
            @ApiResponse(responseCode = "200", description = "OK",content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioAtualizarRequest usuarioAtualizarRequest) {

        log.info("Atualizando usuário: {}", id);
        UsuarioResponse usuarioResponse = usuarioService.atualizar(id, usuarioAtualizarRequest);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponse);

    }

    // --- RetornarUm ---------------------------------------------------------
    @Operation(summary = "Retorna um usuário", responses = {
            @ApiResponse(responseCode = "200", description = "OK",content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> retornarUm(@Parameter(description = "ID do usuário", example = "6cf41e83-ab70-4598-8eb4-354a49b19e3f") @PathVariable UUID id) {

        log.info("Recuperando um usuário: {}", id);
        UsuarioResponse usuarioResponse = usuarioService.retornarUm(id);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioResponse);

    }

    // --- TrocarSenha --------------------------------------------------------
    @Operation(summary = "Trocar senha do usuário", responses = {
            @ApiResponse(responseCode = "204", description = "OK", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @PutMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody TrocaSenhaRequest trocaSenhaRequest) {

        log.info("Trocando senha do usuário");
        usuarioService.trocarSenha(trocaSenhaRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    // --- Inserir ------------------------------------------------------------
    @Operation(summary = "Insere um usuário", responses = {
            @ApiResponse(responseCode = "201", description = "OK",content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorRequestDTO.class)))
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> inserir(@Valid @RequestBody UsuarioRequest usuarioRequest) {

        log.info("Inserindo usuário: {}", usuarioRequest.getEmail());
        UsuarioResponse usuarioResponse = usuarioService.inserir(usuarioRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioResponse);

    }

    // --- AlterarStatus ------------------------------------------------------
    @Operation(summary = "Muda o status do usuário", responses = {
            @ApiResponse(responseCode = "204", description = "Status modificado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDefaultDTO.class)))
    })
    @PutMapping(path = "{id}/alterar-status")
    public ResponseEntity<Void> alterarStatus(@Parameter(description = "ID do usuário", example = "6cf41e83-ab70-4598-8eb4-354a49b19e3f") @PathVariable UUID id) {

        usuarioService.alterarStatus(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
