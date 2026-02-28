package com.project.plutus.exceptions;

public class NotEnoughAmountException extends PlutusStateException {
    private static final String CODE = "not.enough.amount";

    public NotEnoughAmountException(String message) {
        super(CODE, message);
    }
}
