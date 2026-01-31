package com.project.plutus.exceptions;

public class UserDoesNotExistException extends PlutusStateException {
    private static final String CODE = "user.does.not.exist";
    private static final String MESSAGE = "This user doesn't exist";

    public UserDoesNotExistException() {
        super(CODE, MESSAGE);
    }
}
