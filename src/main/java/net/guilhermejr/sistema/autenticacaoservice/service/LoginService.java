package net.guilhermejr.sistema.autenticacaoservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.dto.EsqueciMinhaSenhaDTO;
import net.guilhermejr.sistema.autenticacaoservice.api.request.EsqueciMinhaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.LoginRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.RefreshTokenRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.JWTResponde;
import net.guilhermejr.sistema.autenticacaoservice.client.NotificacaoClient;
import net.guilhermejr.sistema.autenticacaoservice.config.security.JwtProvider;
import net.guilhermejr.sistema.autenticacaoservice.config.security.UserDetailsImpl;
import net.guilhermejr.sistema.autenticacaoservice.config.security.UserDetailsServiceImpl;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Usuario;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.UsuarioRepository;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionDefault;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionNotFound;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
public class LoginService {

    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoClient notificacaoClient;
    private final UserDetailsServiceImpl userDetailsService;

    // --- Login --------------------------------------------------------------
    public JWTResponde login (LoginRequest loginRequest) {

        try {

            // --- Realiza autenticação e grava no contexto ---
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // --- Gera token jwt ---
            String token = jwtProvider.generateToken(authentication);
            log.info("Usuário: {} autenticado com sucesso", loginRequest.getEmail());

            // --- Atualiza último acesso ---
            UserDetailsImpl usuarioLogado = (UserDetailsImpl) authentication.getPrincipal();
            usuarioRepository.findById(usuarioLogado.getId()).ifPresent(usuario -> {
                usuario.setUltimoAcesso(LocalDateTime.now(ZoneId.of("UTC")));
                usuario.setTentativaLogin(0);
                usuarioRepository.save(usuario);
            });

            // --- Gera refreshToken ---
            String refreshToken = jwtProvider.gerarRefreshToken(authentication);

            // --- Retorno ---
            return new JWTResponde(token, refreshToken);

        } catch (Exception e) {

            atualizarTentativaLogin(loginRequest.getEmail());

            log.error("Usuário: {} não informou uma chave email:senha válidos, ou está inativo.", loginRequest.getEmail());
            throw new ExceptionDefault("Combinação de e-mail e senha inválidos.");

        }

    }

    public JWTResponde refreshToken(@Valid RefreshTokenRequest refreshTokenRequest) {

        if (jwtProvider.validateToken(refreshTokenRequest.getRefreshToken())) {
            String email = jwtProvider.getSubjectToken(refreshTokenRequest.getRefreshToken());
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // --- Gera token jwt ---
            String token = jwtProvider.generateToken(authentication);

            // --- Gera refreshToken ---
            String refreshToken = jwtProvider.gerarRefreshToken(authentication);

            // --- Retorno ---
            return new JWTResponde(token, refreshToken);

        } else {
            throw new ExceptionDefault("RefreshToken inválido");
        }

    }

    private void atualizarTentativaLogin(String email) {

        usuarioRepository.findByEmail(email).ifPresentOrElse(usuario -> {
            log.info("Adicionando tentativa de login inválido para o e-mail: {}", email);
            int tentativas = usuario.getTentativaLogin() + 1;
            if (tentativas == 3) {

                usuario.setAtivo(Boolean.FALSE);
            }
            usuario.setTentativaLogin(tentativas);
            usuarioRepository.save(usuario);

        }, () -> {
            log.info("E-mail não existe na base de dados: {}", email);
        });

    }

    // --- EsqueciMinhaSenha --------------------------------------------------
    @Transactional
    public void esqueciMinhaSenha(EsqueciMinhaSenhaRequest esqueciMinhaSenhaRequest) {

        Usuario usuario = usuarioRepository.findByEmail(esqueciMinhaSenhaRequest.getEmail()).orElseThrow(
                () -> new ExceptionNotFound("Usuário: " + esqueciMinhaSenhaRequest.getEmail() + " - Não encontrado")
        );

        String hash = UUID.randomUUID().toString();

        usuario.setRecuperarSenha(hash);

        EsqueciMinhaSenhaDTO esqueciMinhaSenhaDTO = EsqueciMinhaSenhaDTO
                .builder()
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .hash(hash)
                .build();

        try {

            ResponseEntity<Void> re = notificacaoClient.enviarLink(esqueciMinhaSenhaDTO);
            if (re.getStatusCode().is2xxSuccessful()) {
                log.info("Dados de {} gravados na fila para serem processados", esqueciMinhaSenhaRequest.getEmail());
            } else {
                log.error("Error ao gravar dados de {} na fila para semrem processados", esqueciMinhaSenhaRequest.getEmail());
            }

        } catch (Exception e) {

            log.error("Erro ao enviar mensagem para fila - {}", e.getMessage());
            throw new ExceptionDefault("Erro ao enviar mensagem para fila");

        }

    }

    // --- enviaSenhaNova -----------------------------------------------------
//    public void enviaSenhaNova() {
//
//        String novaSenha = UUID.randomUUID().toString();
//        String novaSenhaCriptografada = new BCryptPasswordEncoder().encode(novaSenha);
//        String email = esqueciMinhaSenhaRequest.getEmail();
//        Usuario usuario = usuarioRepository.findByEmail(esqueciMinhaSenhaRequest.getEmail()).orElseThrow(() -> {
//            log.error("Usuário: {} - Não encontrado", email);
//            throw new ExceptionNotFound("Usuário: "+ email +" - Não encontrado");
//        });
//        usuario.setSenha(novaSenhaCriptografada);
//        usuarioRepository.save(usuario);
//
//        EsqueciMinhaSenhaDTO esqueciMinhaSenhaDTO = EsqueciMinhaSenhaDTO
//                .builder()
//                .nome(usuario.getNome())
//                .email(usuario.getEmail())
//                .senha(novaSenha)
//                .build();
//
//        try {
//
//            ResponseEntity<Void> re = notificacaoClient.enviarRecuperarSenhaFila(esqueciMinhaSenhaDTO);
//            if (re.getStatusCode().is2xxSuccessful()) {
//                log.info("Dados de {} gravados na fila para serem processados", esqueciMinhaSenhaDTO.getNome());
//            } else {
//                log.error("Error ao gravar dados de {} na fila para semrem processados", esqueciMinhaSenhaDTO.getNome());
//            }
//
//        } catch (Exception e) {
//
//            log.error("Erro ao enviar mensagem para fila - {}", e.getMessage());
//            throw new ExceptionDefault("Erro ao enviar mensagem para fila");
//
//        }
//    }

}
