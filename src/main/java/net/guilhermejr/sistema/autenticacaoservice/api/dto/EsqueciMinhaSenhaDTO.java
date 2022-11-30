package net.guilhermejr.sistema.autenticacaoservice.api.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EsqueciMinhaSenhaDTO implements Serializable {

    private String nome;
    private String email;
    private String senha;

}
