package com.project.plutus.exceptions;

public class NotEnoughAmountException extends PlutusStateException {
    private static final String CODE = "not.enough.amount";
    private static final String MESSAGE = "Not enough amount to perform this operation";

    public NotEnoughAmountException() {
        super(CODE, MESSAGE);
    }
}
