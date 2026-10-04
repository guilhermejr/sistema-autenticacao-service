package net.guilhermejr.sistema.autenticacaoservice.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoisFatoresConfiguracaoResponse {

    // --- Em Base32, para digitar no app quando o QR code não puder ser lido ---
    private String segredo;

    // --- otpauth://, o conteúdo do QR code ---
    private String uri;

}
