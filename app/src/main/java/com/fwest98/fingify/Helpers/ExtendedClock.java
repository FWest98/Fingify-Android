package com.fwest98.fingify.Helpers;

import org.jboss.aerogear.security.otp.api.Clock;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ExtendedClock extends Clock {
    private final int interval;

    public ExtendedClock() {
        super();

        interval = 30;
    }

    public ExtendedClock(int interval) {
        super(interval);

        this.interval = interval;
    }

    public double getTimeLeft() {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        long currentTimeSeconds = calendar.getTimeInMillis() / 1000;
        int timeLeft = (int) currentTimeSeconds % interval;
        return 1 - ((double) timeLeft / interval);
    }
}