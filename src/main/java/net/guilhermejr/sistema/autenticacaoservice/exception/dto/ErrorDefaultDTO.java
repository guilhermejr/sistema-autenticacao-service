package net.guilhermejr.sistema.autenticacaoservice.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Retorno de erro genérico")
public class ErrorDefaultDTO {

    @Schema(description = "Mensagem de erro", example = "Recurso não encontrado")
    private String mensagem;

}
