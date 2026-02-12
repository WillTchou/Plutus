package com.project.plutus.exceptions;

public class AccountNotFoundException extends PlutusStateException {
    private static final String CODE = "account.does.not.exist";
    private static final String MESSAGE = "This account doesn't exist";

    public AccountNotFoundException() {
        super(CODE, MESSAGE);
    }
}
