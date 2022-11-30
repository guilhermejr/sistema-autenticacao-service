package net.guilhermejr.sistema.autenticacaoservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Request para atualizar usuário")
public class UsuarioAtualizarRequest {

    @NotBlank
    @Schema(description = "Nome", example = "Guilherme Jr.")
    private String nome;

    @NotEmpty
    @Schema(description = "Perfil(s) do usuário", example = "ROLE_ADMIN")
    private List<String> perfis;
}
