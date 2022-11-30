package net.guilhermejr.sistema.autenticacaoservice.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Schema(description = "Retorno do login")
public class JWTResponde {

    @NonNull
    @Schema(description = "Token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI3MGQ3NDBiNy1lNjMyLTRjOTUtYjBhNy1kODI0ZTdhYWNiNGIiLCJwZXJmaXMiOiJST0xFX1JFTUVESU9TLFJPTEVfRU5FUkdJQSxST0xFX0FETUlOLFJPTEVfR0FTVE9TLFJPTEVfU1VQRVJNRVJDQURPLFJPTEVfTElWUk9TIiwiaWF0IjoxNjU1ODM5MzIxLCJleHAiOjE2NTU4NTM3MjF9.GHTMnBgHHUth3c1r8TDEEYcMd4qAArhUaYkX1N4qVObtc4y6aqsFI3mun5Fyza4gbIUQa_-9GShcnbNItqu0UA")
    private String token;

    @Schema(description = "Tipo", example = "Bearer")
    private String tipo = "Bearer";

}
