package net.guilhermejr.sistema.autenticacaoservice.api.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Request para trocar senha do usuário")
public class TrocaSenhaRequest {

    @Schema(description = "Senha atual", example = "C4m1l4@@")
    @NotBlank()
    private String senhaAtual;

    @Schema(description = "Nova senha", example = "!C4m1l4@@!")
    @NotBlank()
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    private String senhaNova;

    @Schema(description = "Confirmar nova senha", example = "!C4m1l4@@!")
    @NotBlank()
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    private String senhaNovaConfirmar;

}
