package org.ikigaidigital.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String message, Throwable exception) {
        super(message, exception);
    }
}
