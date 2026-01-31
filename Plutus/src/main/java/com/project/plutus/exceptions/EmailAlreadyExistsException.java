package com.project.plutus.exceptions;

public class EmailAlreadyExistsException extends PlutusStateException {
    private static final String CODE = "email.already.exists";
    private static final String MESSAGE = "This email is already registered";

    public EmailAlreadyExistsException() {
        super(CODE, MESSAGE);
    }
}
