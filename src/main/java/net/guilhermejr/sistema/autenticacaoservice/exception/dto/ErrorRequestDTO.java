package net.guilhermejr.sistema.autenticacaoservice.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Retorno de erro de request")
public class ErrorRequestDTO {

    @Schema(description = "Campo com erro", example = "inicio")
    private String campo;

    @Schema(description = "Mensagem de erro", example = "Início deve ser uma data")
    private String mensagem;

}
