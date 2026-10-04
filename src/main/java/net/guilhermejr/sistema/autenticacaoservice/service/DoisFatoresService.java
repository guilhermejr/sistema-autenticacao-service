package net.guilhermejr.sistema.autenticacaoservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.request.CodigoDoisFatoresRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.DoisFatoresConfiguracaoResponse;
import net.guilhermejr.sistema.autenticacaoservice.api.response.DoisFatoresStatusResponse;
import net.guilhermejr.sistema.autenticacaoservice.config.security.AuthenticationCurrentUserService;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Usuario;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.UsuarioRepository;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionDefault;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionNotFound;
import org.springframework.stereotype.Service;

import java.util.OptionalLong;

@Log4j2
@RequiredArgsConstructor
@Service
public class DoisFatoresService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationCurrentUserService authenticationCurrentUserService;
    private final TotpService totpService;
    private final LoginService loginService;

    // --- Status -------------------------------------------------------------
    public DoisFatoresStatusResponse status() {

        return new DoisFatoresStatusResponse(Boolean.TRUE.equals(usuarioLogado().getDoisFatoresAtivo()));

    }

    // --- Configurar ---------------------------------------------------------
    // --- Gera um segredo novo, que só passa a valer depois de ativar com um código ---
    public DoisFatoresConfiguracaoResponse configurar() {

        Usuario usuario = usuarioLogado();
        if (Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())) {
            throw new ExceptionDefault("Dois fatores já está ativo. Desative antes de configurar de novo.");
        }

        String segredo = totpService.gerarSegredo();
        usuario.setDoisFatoresSegredo(segredo);
        usuario.setDoisFatoresUltimoPasso(null);
        usuarioRepository.save(usuario);

        log.info("Usuário: {} gerou segredo de dois fatores", usuario.getEmail());
        return new DoisFatoresConfiguracaoResponse(totpService.segredoBase32(segredo), totpService.uri(usuario.getEmail(), segredo));

    }

    // --- Ativar -------------------------------------------------------------
    public void ativar(CodigoDoisFatoresRequest codigoDoisFatoresRequest) {

        Usuario usuario = usuarioLogado();
        if (Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())) {
            throw new ExceptionDefault("Dois fatores já está ativo.");
        }
        if (usuario.getDoisFatoresSegredo() == null) {
            throw new ExceptionDefault("Configure o autenticador antes de ativar.");
        }

        OptionalLong passo = totpService.validar(usuario.getDoisFatoresSegredo(), codigoDoisFatoresRequest.getCodigo(), usuario.getDoisFatoresUltimoPasso());
        if (passo.isEmpty()) {
            throw new ExceptionDefault("Código inválido. Confira se o relógio do celular está certo.");
        }

        usuario.setDoisFatoresUltimoPasso(passo.getAsLong());
        usuario.setDoisFatoresAtivo(Boolean.TRUE);
        usuarioRepository.save(usuario);
        log.info("Usuário: {} ativou dois fatores", usuario.getEmail());

    }

    // --- Desativar ----------------------------------------------------------
    // --- Exige o código: um access token roubado não basta para desligar ---
    public void desativar(CodigoDoisFatoresRequest codigoDoisFatoresRequest) {

        Usuario usuario = usuarioLogado();
        if (!Boolean.TRUE.equals(usuario.getDoisFatoresAtivo())) {
            throw new ExceptionDefault("Dois fatores não está ativo.");
        }

        OptionalLong passo = totpService.validar(usuario.getDoisFatoresSegredo(), codigoDoisFatoresRequest.getCodigo(), usuario.getDoisFatoresUltimoPasso());
        if (passo.isEmpty()) {
            loginService.atualizarTentativaLogin(usuario.getEmail());
            throw new ExceptionDefault("Código inválido.");
        }

        usuario.setDoisFatoresAtivo(Boolean.FALSE);
        usuario.setDoisFatoresSegredo(null);
        usuario.setDoisFatoresUltimoPasso(null);
        usuarioRepository.save(usuario);
        log.info("Usuário: {} desativou dois fatores", usuario.getEmail());

    }

    private Usuario usuarioLogado() {
        return usuarioRepository.findById(authenticationCurrentUserService.getCurrentUser().getId()).orElseThrow(
                () -> new ExceptionNotFound("Usuário não encontrado")
        );
    }

}
