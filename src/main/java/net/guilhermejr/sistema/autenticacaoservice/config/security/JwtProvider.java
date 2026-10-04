package net.guilhermejr.sistema.autenticacaoservice.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Component
public class JwtProvider {

    private static final String ORIENTACAO_SEGREDO =
            "sistema.auth.jwtSecret deve ser uma string Base64 que decodifique para no mínimo 64 bytes "
            + "(exigência do HS512). Gere um novo com: openssl rand -base64 64";

    @Value("${sistema.auth.jwtSecret}")
    private String jwtSecret;

    @Value("${sistema.auth.jwtExpirationMs}")
    private long jwtExpirationMs;

    @Value("${sistema.auth.jwtRefreshTokenExpirationMs}")
    private long jwtRefreshTokenExpirationMs;

    // --- Validade do token entre a senha e o código do autenticador ---
    private static final long DOIS_FATORES_EXPIRACAO_MS = 5 * 60 * 1000;

    private SecretKey chave;

    // --- Assina só o token de dois fatores. Os demais serviços validam com a chave
    // --- principal e montam o usuário só pelas claims, então esse token não pode
    // --- ser assinado com ela: lá ele valeria como access token sem o código. ---
    private SecretKey chaveDoisFatores;

    @PostConstruct
    void inicializarChave() {
        byte[] segredo = decodificarSegredo();
        this.chave = Keys.hmacShaKeyFor(segredo);
        this.chaveDoisFatores = Keys.hmacShaKeyFor(derivarChave(segredo, "dois-fatores"));
    }

    private byte[] derivarChave(byte[] segredo, String finalidade) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(segredo, "HmacSHA512"));
            return mac.doFinal(finalidade.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA512 indisponível", e);
        }
    }

    private byte[] decodificarSegredo() {
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(jwtSecret);
        } catch (DecodingException e) {
            throw new IllegalStateException(ORIENTACAO_SEGREDO
                    + " O valor atual não é Base64 válido.", e);
        }
        if (bytes.length < 64) {
            throw new IllegalStateException(ORIENTACAO_SEGREDO
                    + " O valor atual decodifica para apenas " + bytes.length + " bytes.");
        }
        return bytes;
    }

    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        final String perfis = userPrincipal.getPerfis().stream().map(perfil -> perfil.getAuthority()).collect(Collectors.joining(","));

        return Jwts.builder()
                .subject((userPrincipal.getId().toString()))
                .claim("nome", userPrincipal.getNome())
                .claim("email", userPrincipal.getEmail())
                .claim("perfis", perfis)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(chave, Jwts.SIG.HS512)
                .compact();
    }

    public String gerarRefreshToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject((userPrincipal.getEmail()))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtRefreshTokenExpirationMs))
                .signWith(chave, Jwts.SIG.HS512)
                .compact();

    }

    public String gerarTokenDoisFatores(UUID idUsuario) {

        return Jwts.builder()
                .subject(idUsuario.toString())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + DOIS_FATORES_EXPIRACAO_MS))
                .signWith(chaveDoisFatores, Jwts.SIG.HS512)
                .compact();

    }

    public Optional<UUID> validarTokenDoisFatores(String token) {
        try {
            String subject = Jwts.parser().verifyWith(chaveDoisFatores).build().parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(UUID.fromString(subject));
        } catch (Exception e) {
            log.error("Token de dois fatores inválido: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public String getSubjectToken(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(chave).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Assinatura JWT inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT não suportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
