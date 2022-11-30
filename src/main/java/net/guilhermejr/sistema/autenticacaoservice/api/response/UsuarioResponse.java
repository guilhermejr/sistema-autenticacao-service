package net.guilhermejr.sistema.autenticacaoservice.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Perfil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Retorno de usuário")
public class UsuarioResponse {

    @Schema(description = "Id", example = "bf46be73-815e-410b-b787-cb48c35f8b1c")
    private UUID id;

    @Schema(description = "Nome", example = "Guilherme Jr.")
    private String nome;

    @Schema(description = "E-mail", example = "falecom@guilhermejr.net")
    private String email;

    @Schema(description = "Ativo", example = "true")
    private Boolean ativo = Boolean.TRUE;

    @Schema(description = "Data de criação", example = "2022-06-04T17:13:47.218729")
    private LocalDateTime criado;

    @Schema(description = "Data da última atualização", example = "2022-06-04T17:13:47.218729")
    private LocalDateTime atualizado;

    @Schema(description = "Data do último acesso", example = "2022-06-04T17:13:47.218729")
    private LocalDateTime ultimoAcesso;

    @Schema(description = "Usuário que realizou o cadastro")
    private UsuarioResumidoResponse usuario;

    @Schema(description = "Perfis")
    private List<Perfil> perfis = new ArrayList<>();

}
