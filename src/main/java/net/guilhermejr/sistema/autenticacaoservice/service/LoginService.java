package net.guilhermejr.sistema.autenticacaoservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final QueueMessagingTemplate queueMessagingTemplate;

    @Value("${cloud.aws.fila.esqueci-minha-senha}")
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
            Usuario usuarioDB = usuarioRepository.findById(usuarioLogado.getId()).get();
            usuarioDB.setUltimoAcesso(LocalDateTime.now(ZoneId.of("UTC")));
            usuarioRepository.save(usuarioDB);

            // --- Retorno ---
            return new JWTResponde(jwt);

        } catch (Exception e) {

            log.error("Usuário: {} não informou uma chave email:senha válidos, ou está inativo.", loginRequest.getEmail());
            throw new ExceptionDefault("Combinação de e-mail e senha inválidos.");

        }

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

        queueMessagingTemplate.send(esqueciMinhaSenha, MessageBuilder.withPayload(msg).build());

        log.info("Nova senha enviada para {}", email);

    }
}
