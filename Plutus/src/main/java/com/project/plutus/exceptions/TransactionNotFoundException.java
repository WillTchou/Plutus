package com.project.plutus.exceptions;

public class TransactionNotFoundException extends PlutusStateException {
    private static final String CODE = "transaction.does.not.exist";
    private static final String MESSAGE = "This transaction doesn't exist";

    public TransactionNotFoundException() {
        super(CODE, MESSAGE);
    }
}
