package net.guilhermejr.sistema.autenticacaoservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Request para login")
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(description = "E-mail", example = "falecom@guilhermejr.net")
    private String email;

    @NotBlank
    @Schema(description = "Senha", example = "Teste123@")
    private String senha;

}
