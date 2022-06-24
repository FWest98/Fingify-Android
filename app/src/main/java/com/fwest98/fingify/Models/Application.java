package com.fwest98.fingify.Models;

import android.content.Context;
import android.net.Uri;

import com.fwest98.fingify.Data.AuthenticationType;
import com.fwest98.fingify.R;
import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

import lombok.Getter;

public class Application implements Serializable {
    private static final long serialVersionUID = 7865678;

    @DatabaseField(generatedId = true) @Getter private int id;

    @DatabaseField @Getter private String label;
    @DatabaseField @Getter private String secret;
    @DatabaseField @Getter private String user;
    @DatabaseField @Getter private AuthenticationType type;

    public Application() {}

    public Application(String label, String secret, String user) {
        this(label, secret, user, AuthenticationType.TOTP);
    }

    public Application(String label, String secret, String user, AuthenticationType type) {
        this.label = label;
        this.secret = secret;
        this.type = type;
        this.user = user;
    }

    /**
     * Parse and check an URI from a QR code
     * @param uriString The URI from the QR code
     * @param context The context
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

    public String generateUri() {
        Uri uri = new Uri.Builder()
                .scheme("otpauth")
                .authority("totp")
                .path("/" + user)
                .appendQueryParameter("secret", secret)
                .appendQueryParameter("issue", label)
                .build();
        return uri.toString();
    }
}
