package net.guilhermejr.sistema.autenticacaoservice.api.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class EsqueciMinhaSenhaRequest {

    @NotBlank
    @Email
    private String email;

}
