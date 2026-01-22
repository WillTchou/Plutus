package com.project.plutus.model;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class CustomIdempotenceyKey implements IdentifierGenerator {

    private static int counter = 1;

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        final String prefix = "tx_";
        String suffix = String.format("05%d", counter++);
        return prefix + suffix;
    }
}
