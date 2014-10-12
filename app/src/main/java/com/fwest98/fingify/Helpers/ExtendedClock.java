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
        long timeLeft = getTimeToNextValue();
        return 1 - ((double) timeLeft / (1000 * interval));
    }

    public long getTimeToNextValue() {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        long currentTimeSeconds = calendar.getTimeInMillis();
        return currentTimeSeconds % (interval * 1000);
    }
}