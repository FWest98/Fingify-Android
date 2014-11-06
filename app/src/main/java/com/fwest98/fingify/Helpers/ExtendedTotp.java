package com.fwest98.fingify.Helpers;

import android.content.Context;
import android.net.Uri;

import com.fwest98.fingify.Data.Application;
import com.fwest98.fingify.R;

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

    /**
     * Parse and check an URI from a QR code
     * @param uriString The URI from the QR code
     * @param context
     * @return An application object
     * @throws IllegalArgumentException The error that occurred
     */
    public static Application parseUri(String uriString, Context context) throws IllegalArgumentException {
        Uri uri = Uri.parse(uriString);
        String scheme = uri.getScheme().toLowerCase();
        String path = uri.getPath();
        String authority = uri.getAuthority();
        String user;
        String secret;
        String issuer;

        if(!"otpauth".equals(scheme)) throw new IllegalArgumentException(context.getString(R.string.totp_parse_scheme_invalid));
        if(!"totp".equals(authority)) throw new IllegalArgumentException(context.getString(R.string.totp_parse_authority_invalid), new UnsupportedOperationException(context.getString(R.string.totp_parse_only_totp_accepted)));

        if(path == null || !path.startsWith("/")) throw new IllegalArgumentException(context.getString(R.string.totp_parse_missing_userid));
        user = path.substring(1).trim();
        if(user.length() == 0) throw new IllegalArgumentException(context.getString(R.string.totp_parse_missing_userid));

        secret = uri.getQueryParameter("secret");
        if("".equals(secret)) throw new IllegalArgumentException(context.getString(R.string.totp_parse_secret_missing));

        issuer = uri.getQueryParameter("issuer");

        return new Application(issuer, secret, user);
    }
}
