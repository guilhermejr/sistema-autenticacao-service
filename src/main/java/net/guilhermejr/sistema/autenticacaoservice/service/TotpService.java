package net.guilhermejr.sistema.autenticacaoservice.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.OptionalLong;

/**
 * TOTP (RFC 6238) nos parâmetros que o Google Authenticator entende:
 * HMAC-SHA1, passos de 30 segundos e códigos de 6 dígitos.
 */
@Service
public class TotpService {

    private static final String EMISSOR = "Sistema";
    private static final int TAMANHO_SEGREDO = 20;
    private static final long DURACAO_PASSO_SEGUNDOS = 30;
    private static final int DIGITOS = 6;
    private static final int MODULO = 1_000_000;

    // --- Aceita um passo antes e um depois, para relógio do celular adiantado ou atrasado ---
    private static final int TOLERANCIA_PASSOS = 1;

    private static final char[] ALFABETO_BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private final SecureRandom random = new SecureRandom();

    // --- GerarSegredo -------------------------------------------------------
    public String gerarSegredo() {
        byte[] segredo = new byte[TAMANHO_SEGREDO];
        random.nextBytes(segredo);
        return Base64.getEncoder().encodeToString(segredo);
    }

    // --- SegredoBase32 ------------------------------------------------------
    // --- Formato que o usuário digita no app quando não consegue ler o QR code ---
    public String segredoBase32(String segredo) {
        byte[] bytes = Base64.getDecoder().decode(segredo);
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                sb.append(ALFABETO_BASE32[(buffer >> (bits - 5)) & 0x1f]);
                bits -= 5;
            }
        }
        if (bits > 0) {
            sb.append(ALFABETO_BASE32[(buffer << (5 - bits)) & 0x1f]);
        }
        return sb.toString();
    }

    // --- Uri ----------------------------------------------------------------
    public String uri(String email, String segredo) {
        String rotulo = URLEncoder.encode(EMISSOR + ":" + email, StandardCharsets.UTF_8).replace("+", "%20");
        return "otpauth://totp/" + rotulo
                + "?secret=" + segredoBase32(segredo)
                + "&issuer=" + EMISSOR
                + "&algorithm=SHA1&digits=" + DIGITOS
                + "&period=" + DURACAO_PASSO_SEGUNDOS;
    }

    // --- Validar ------------------------------------------------------------
    // --- Devolve o passo que casou, ou vazio. Passos até ultimoPassoUsado são recusados (replay) ---
    public OptionalLong validar(String segredo, String codigo, Long ultimoPassoUsado) {

        if (segredo == null || codigo == null || !codigo.matches("\\d{" + DIGITOS + "}")) {
            return OptionalLong.empty();
        }

        byte[] chave = Base64.getDecoder().decode(segredo);
        byte[] informado = codigo.getBytes(StandardCharsets.US_ASCII);
        long passoAtual = Instant.now().getEpochSecond() / DURACAO_PASSO_SEGUNDOS;

        for (long passo = passoAtual - TOLERANCIA_PASSOS; passo <= passoAtual + TOLERANCIA_PASSOS; passo++) {
            if (ultimoPassoUsado != null && passo <= ultimoPassoUsado) {
                continue;
            }
            byte[] esperado = gerarCodigo(chave, passo).getBytes(StandardCharsets.US_ASCII);
            if (MessageDigest.isEqual(esperado, informado)) {
                return OptionalLong.of(passo);
            }
        }
        return OptionalLong.empty();

    }

    private String gerarCodigo(byte[] chave, long passo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(chave, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(passo).array());
            int deslocamento = hash[hash.length - 1] & 0x0f;
            int binario = ((hash[deslocamento] & 0x7f) << 24)
                    | ((hash[deslocamento + 1] & 0xff) << 16)
                    | ((hash[deslocamento + 2] & 0xff) << 8)
                    | (hash[deslocamento + 3] & 0xff);
            return String.format("%0" + DIGITOS + "d", binario % MODULO);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA1 indisponível", e);
        }
    }

}
