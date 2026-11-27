package com.fiap.mekano.registry.domain.exception;

/**
 * Excecao padrao da aplicacao. Carrega o status HTTP e a mensagem que devem
 * ser traduzidos pela camada REST.
 */
public class AppException extends RuntimeException {

    private final int status;

    public AppException(int status, String message) {
        super(message);
        this.status = status;
    }

    public AppException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
