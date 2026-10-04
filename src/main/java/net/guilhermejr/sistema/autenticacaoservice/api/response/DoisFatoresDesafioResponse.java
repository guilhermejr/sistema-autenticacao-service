package net.guilhermejr.sistema.autenticacaoservice.api.response;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DoisFatoresDesafioResponse implements LoginResponse {

    private final boolean doisFatores = true;

    @NonNull
    private String tokenDoisFatores;

}
