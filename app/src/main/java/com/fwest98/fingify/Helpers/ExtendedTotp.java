package com.fwest98.fingify.Helpers;

import org.jboss.aerogear.security.otp.Totp;

public class ExtendedTotp extends Totp {
    private final ExtendedClock clock;

    public ExtendedTotp(String secret) {
        super(secret);
        clock = new ExtendedClock();
    }

    public ExtendedTotp(String secret, ExtendedClock clock) {
        super(secret, clock);
        this.clock = clock;
    }

    public ExtendedClock getClock() {
        return clock;
    }
}
