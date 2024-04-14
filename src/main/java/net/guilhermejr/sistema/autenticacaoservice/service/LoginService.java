package net.guilhermejr.sistema.autenticacaoservice.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.dto.EsqueciMinhaSenhaDTO;
import net.guilhermejr.sistema.autenticacaoservice.api.request.EsqueciMinhaSenhaRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.request.LoginRequest;
import net.guilhermejr.sistema.autenticacaoservice.api.response.JWTResponde;
import net.guilhermejr.sistema.autenticacaoservice.config.security.JwtProvider;
import net.guilhermejr.sistema.autenticacaoservice.config.security.UserDetailsImpl;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Usuario;
import net.guilhermejr.sistema.autenticacaoservice.domain.repository.UsuarioRepository;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionDefault;
import net.guilhermejr.sistema.autenticacaoservice.exception.ExceptionNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
public class LoginService {

    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final AmazonSQS amazonSQSClient;

    @Value("${cloud.aws.fila.esqueci-minha-senha.url}")
    private String esqueciMinhaSenha;

    // --- Login --------------------------------------------------------------
    public JWTResponde login (LoginRequest loginRequest) {

        try {

            // --- Realiza autenticação e grava no contexto ---
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // --- Gera token jwt ---
            String jwt = jwtProvider.generateJwt(authentication);
            log.info("Usuário: {} autenticado com sucesso", loginRequest.getEmail());

            // --- Atualiza último acesso ---
            UserDetailsImpl usuarioLogado = (UserDetailsImpl) authentication.getPrincipal();
            usuarioRepository.findById(usuarioLogado.getId()).ifPresent(usuario -> {
                usuario.setUltimoAcesso(LocalDateTime.now(ZoneId.of("UTC")));
                usuario.setTentativaLogin(0);
                usuarioRepository.save(usuario);
            });

            // --- Retorno ---
            return new JWTResponde(jwt);

        } catch (Exception e) {

            atualizarTentativaLogin(loginRequest.getEmail());

            log.error("Usuário: {} não informou uma chave email:senha válidos, ou está inativo.", loginRequest.getEmail());
            throw new ExceptionDefault("Combinação de e-mail e senha inválidos.");

        }

    }

    private void atualizarTentativaLogin(String email) {

        usuarioRepository.findByEmail(email).ifPresentOrElse(usuario -> {
            log.info("Adicionando tentativa de login inválido para o e-mail: {}", email);
            int tentativas = usuario.getTentativaLogin() + 1;
            if (tentativas >= 3) {
                usuario.setAtivo(Boolean.FALSE);
            }
            usuario.setTentativaLogin(tentativas);
            usuarioRepository.save(usuario);

        }, () -> {
            log.info("E-mail não existe na base de dados: {}", email);
        });

    }

    // --- EsqueciMinhaSenha --------------------------------------------------
    public void esqueciMinhaSenha(EsqueciMinhaSenhaRequest esqueciMinhaSenhaRequest) {

        String novaSenha = UUID.randomUUID().toString();
        String novaSenhaCriptografada = new BCryptPasswordEncoder().encode(novaSenha);
        String email = esqueciMinhaSenhaRequest.getEmail();
        Usuario usuario = usuarioRepository.findByEmail(esqueciMinhaSenhaRequest.getEmail()).orElseThrow(() -> {
            log.error("Usuário: {} - Não encontrado", email);
            throw new ExceptionNotFound("Usuário: "+ email +" - Não encontrado");
        });
        usuario.setSenha(novaSenhaCriptografada);
        usuarioRepository.save(usuario);

        EsqueciMinhaSenhaDTO esqueciMinhaSenhaDTO = EsqueciMinhaSenhaDTO.builder().nome(usuario.getNome()).email(usuario.getEmail()).senha(novaSenha).build();

        ObjectMapper mapper = new ObjectMapper();
        String msg = null;
        try {
            msg = mapper.writeValueAsString(esqueciMinhaSenhaDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {

            amazonSQSClient.sendMessage(esqueciMinhaSenha, msg);
            log.info("Dados de {} gravados na fila para serem processados", esqueciMinhaSenhaDTO.getNome());

        } catch (Exception e) {

            log.error("Erro ao enviar mensagem para fila - {}", e.getMessage());
            throw new ExceptionDefault("Erro ao enviar mensagem para fila");

        }

    }
}
