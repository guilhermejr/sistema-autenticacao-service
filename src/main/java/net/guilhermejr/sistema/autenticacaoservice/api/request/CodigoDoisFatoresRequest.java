package net.guilhermejr.sistema.autenticacaoservice.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CodigoDoisFatoresRequest {

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String codigo;

}
