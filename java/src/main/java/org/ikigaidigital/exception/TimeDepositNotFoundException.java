package org.ikigaidigital.exception;

public class TimeDepositNotFoundException extends RuntimeException {

    public TimeDepositNotFoundException(String message, Throwable exception) {
        super(message, exception);
    }
}
