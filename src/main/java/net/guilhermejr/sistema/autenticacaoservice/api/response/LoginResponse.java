package net.guilhermejr.sistema.autenticacaoservice.api.response;

/**
 * Resposta do POST /login: os tokens ({@link JWTResponde}) ou, com dois fatores
 * ativo, o desafio a ser respondido em /login/dois-fatores
 * ({@link DoisFatoresDesafioResponse}).
 */
public interface LoginResponse {
}
