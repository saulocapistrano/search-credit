package br.com.searchcredit.application.exception;

public class CreditoNotFoundException extends RuntimeException {

    public CreditoNotFoundException(String message) {
        super(message);
    }
}
