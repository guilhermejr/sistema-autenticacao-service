package net.guilhermejr.sistema.autenticacaoservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "Request para esqueci minha senha")
public class EsqueciMinhaSenhaRequest {

    @NotBlank
    @Email
    @Schema(description = "E-mail", example = "falecom@guilhermejr.net")
    private String email;

}
