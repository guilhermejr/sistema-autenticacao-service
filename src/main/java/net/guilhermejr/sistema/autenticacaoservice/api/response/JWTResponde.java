package net.guilhermejr.sistema.autenticacaoservice.api.response;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JWTResponde {

    @NonNull
    private String token;
    private String tipo = "Bearer";

}
