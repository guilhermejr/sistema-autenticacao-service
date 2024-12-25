package net.guilhermejr.sistema.autenticacaoservice.api.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsuarioAtualizarRequest {

    @NotBlank
    private String nome;

    @NotEmpty
    private List<String> perfis;
}
