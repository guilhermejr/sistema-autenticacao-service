package net.guilhermejr.sistema.autenticacaoservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.mapper.UsuarioMapper;
import net.guilhermejr.sistema.autenticacaoservice.api.request.TrocaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.UsuarioAtualizarRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.UsuarioRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.UsuarioResponse;
import net.guilhermejr.sistema.autenticacaoservice.config.security.AuthenticationCurrentUserService;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Perfil;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Usuario;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.PerfilRepository;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.UsuarioRepository;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionDefault;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final AuthenticationCurrentUserService authenticationCurrentUserService;

    // --- Inserir ------------------------------------------------------------
    @Transactional
    public UsuarioResponse inserir(UsuarioRequest usuarioRequest) {

        if (!usuarioRequest.getSenha().equals(usuarioRequest.getConfirmarSenha())) {
            log.error("Usuário: {} - Senha e Confirmar senha devem ser iguais", usuarioRequest.getEmail());
            throw new ExceptionDefault("Senha e Confirmar senha devem ser iguais");
        }

        Usuario usuarioLogado = usuarioMapper.mapUserDetailsImpl(authenticationCurrentUserService.getCurrentUser());
        Usuario usuario = usuarioMapper.mapObject(usuarioRequest);

        // --- Perfil ---
        List<Perfil> perfis = new ArrayList<>();
        usuario.getPerfis().forEach(p -> {
            Perfil perfil = perfilRepository.findByDescricao(p.getDescricao())
                    .orElseThrow(() -> {
                        log.error("Usuário: {} - Perfil não encontrado", usuarioRequest.getEmail());
                        return new ExceptionNotFound("Perfil não encontrado");
                    });
            perfis.add(perfil);
        });
        usuario.setPerfis(perfis);
        usuario.setUsuario(usuarioLogado);

        // --- Criptografa senha ---
        usuario.setSenha(new BCryptPasswordEncoder().encode(usuarioRequest.getSenha()));

        // --- Inclui as datas de criação e atualização ---
        usuario.setCriado(LocalDateTime.now(ZoneId.of("UTC")));
        usuario.setAtualizado(LocalDateTime.now(ZoneId.of("UTC")));

        // --- Salva usuário novo ---
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        log.info("Usuário: {} - Salvo com sucesso", usuarioSalvo.getEmail());

        return usuarioMapper.mapObject(usuarioSalvo);

    }

    // --- Retornar -----------------------------------------------------------
    public Page<UsuarioResponse> retornar(Pageable paginacao) {

        Page<Usuario> usuarios = usuarioRepository.findAll(paginacao);
        return usuarioMapper.mapPage(usuarios);

    }

    // --- TrocarSenha --------------------------------------------------------
    @Transactional
    public void trocarSenha(TrocaSenhaRequest trocaSenhaRequest) {

        Usuario usuarioLogado = usuarioMapper.mapUserDetailsImpl(authenticationCurrentUserService.getCurrentUser());

        if (!trocaSenhaRequest.getSenhaNova().equals(trocaSenhaRequest.getSenhaNovaConfirmar())) {
            log.error("Usuário: {} - Nova senha e Confirmar nova senha devem ser iguais", usuarioLogado.getEmail());
            throw new ExceptionDefault("Nova senha e Confirmar nova senha devem ser iguais");
        }

        Usuario usuario = this.usuarioRepository.findById(usuarioLogado.getId()).get();

        if (!new BCryptPasswordEncoder().matches(trocaSenhaRequest.getSenhaAtual(), usuario.getSenha())) {
            log.error("Usuário: {} - Senha atual está errada", usuarioLogado.getEmail());
            throw new ExceptionDefault("Senha atual está errada");
        }

        usuario.setSenha(new BCryptPasswordEncoder().encode(trocaSenhaRequest.getSenhaNova()));
        this.usuarioRepository.save(usuario);

        log.info("Usuário: {} - Trocado senha com sucesso", usuarioLogado.getEmail());

    }

    // --- Atualizar ----------------------------------------------------------
    public UsuarioResponse atualizar(UUID id, UsuarioAtualizarRequest usuarioAtualizarRequest) {

        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> {
            log.error("Usuário: {} - Não encontrado", id);
            throw new ExceptionNotFound("Usuário: "+ id +" - Não encontrado");
        });
        Usuario usuarioLogado = usuarioMapper.mapUserDetailsImpl(authenticationCurrentUserService.getCurrentUser());

        // --- Perfil ---
        List<Perfil> perfis = new ArrayList<>();
        usuarioAtualizarRequest.getPerfis().forEach(p -> {
            Perfil perfil = perfilRepository.findByDescricao(p)
                    .orElseThrow(() -> {
                        log.error("Usuário: {} - Perfil não encontrado", usuario.getEmail());
                        return new ExceptionNotFound("Perfil não encontrado");
                    });
            perfis.add(perfil);
        });

        // --- Atualiza os dados ---
        usuario.setNome(usuarioAtualizarRequest.getNome());
        usuario.setPerfis(perfis);
        usuario.setUsuario(usuarioLogado);
        usuario.setAtualizado(LocalDateTime.now(ZoneId.of("UTC")));

        usuarioRepository.save(usuario);

        return usuarioMapper.mapObject(usuario);

    }

    // --- RetornarUm ---------------------------------------------------------
    public UsuarioResponse retornarUm(UUID id) {

        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> {
            log.error("Usuário: {} - Não encontrado", id);
            throw new ExceptionNotFound("Usuário: "+ id +" - Não encontrado");
        });

        return usuarioMapper.mapObject(usuario);

    }

    // --- alterarStatus ------------------------------------------------------
    @Transactional
    public void alterarStatus(UUID id) {

        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> {
            log.error("Usuário: {} - Não encontrado", id);
            throw new ExceptionNotFound("Usuário: "+ id +" - Não encontrado");
        });

        usuario.setAtivo(!usuario.getAtivo());

    }

}
