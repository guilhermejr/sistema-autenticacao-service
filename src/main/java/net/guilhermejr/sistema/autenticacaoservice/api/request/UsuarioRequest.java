package net.guilhermejr.sistema.autenticacaoservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import net.guilhermejr.sistema.autenticacaoservice.api.request.validation.constrant.UsuarioUnico;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Request para inserir usuário")
public class UsuarioRequest {

    @NotBlank
    @Schema(description = "Nome", example = "Guilherme Jr.")
    private String nome;

    @NotBlank
    @UsuarioUnico
    @Email
    @Schema(description = "E-mail", example = "falecom@guilhermejr.net")
    private String email;

    @Schema(description = "Senha do usuário", example = "C4m1l@!!")
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    private String senha;

    @Schema(description = "Confirmação da senha do usuário", example = "C4m1l@!!")
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    private String confirmarSenha;

    @NotEmpty
    @Schema(description = "Perfil(s) do usuário", example = "ROLE_ADMIN")
    private List<String> perfis;

}
