package net.guilhermejr.sistema.autenticacaoservice.exception;

public class ExceptionNotFound extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExceptionNotFound(String mensagem){
        super(mensagem);
    }

}
