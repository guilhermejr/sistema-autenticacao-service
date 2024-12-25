package net.guilhermejr.sistema.autenticacaoservice.api.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsuarioResumidoResponse {

    private UUID id;
    private String nome;

}
