package com.akinobank.app.utilities;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

public class VerificationTokenGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return generateVerificationToken();
    }

    public static String generateVerificationToken () {
        return
            UUID.randomUUID().toString()
            .concat(UUID.randomUUID().toString())
            .concat(UUID.randomUUID().toString())
            .concat(UUID.randomUUID().toString())
            .concat(UUID.randomUUID().toString())
                .replace("-", "");
    }
}
