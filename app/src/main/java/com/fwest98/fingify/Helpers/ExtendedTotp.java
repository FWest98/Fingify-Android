package com.fwest98.fingify.Helpers;

import org.jboss.aerogear.security.otp.Totp;

public class ExtendedTotp extends Totp {
    private final ExtendedClock clock;
    private long timeToNext;

    public ExtendedTotp(String secret) {
        super(secret);
        clock = new ExtendedClock();
        timeToNext = clock.getTimeToNextValue();
    }

    public ExtendedTotp(String secret, ExtendedClock clock) {
        super(secret, clock);
        this.clock = clock;
        timeToNext = clock.getTimeToNextValue();
    }

    public double getTimeLeft() {
        return clock.getTimeLeft();
    }

    public long getTimeToNextValue() {
        return clock.getTimeToNextValue();
    }

    public boolean isChanged() {
        boolean isChanged = (timeToNext - clock.getTimeToNextValue()) < 0 || clock.getTimeToNextValue() == 0;
        timeToNext = clock.getTimeToNextValue();
        return isChanged;
    }
}
