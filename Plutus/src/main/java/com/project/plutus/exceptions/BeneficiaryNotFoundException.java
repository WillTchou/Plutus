package com.project.plutus.exceptions;

public class BeneficiaryNotFoundException extends PlutusStateException {
    private static final String CODE = "beneficiary.does.not.exist";
    private static final String MESSAGE = "This beneficiary doesn't exist";

    public BeneficiaryNotFoundException() {
        super(CODE, MESSAGE);
    }
}

