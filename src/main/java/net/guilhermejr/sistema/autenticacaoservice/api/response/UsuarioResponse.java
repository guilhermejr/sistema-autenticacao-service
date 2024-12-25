package net.guilhermejr.sistema.autenticacaoservice.api.response;

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
public class UsuarioResponse {

    private UUID id;
    private String nome;
    private String email;
    private Boolean ativo = Boolean.TRUE;
    private LocalDateTime criado;
    private LocalDateTime atualizado;
    private LocalDateTime ultimoAcesso;
    private List<Perfil> perfis = new ArrayList<>();

}
