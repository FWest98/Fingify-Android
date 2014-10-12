package com.fwest98.fingify.Data;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

import lombok.Getter;

public class Application implements Serializable {
    private static final long serialVersionUID = 7865678;

    @DatabaseField(generatedId = true) private int id;

    @DatabaseField @Getter private String label;
    @DatabaseField @Getter private String secret;
    @DatabaseField @Getter private AuthenticationType type;

    public Application() {}

    public Application(String label, String secret) {
        this.label = label;
        this.secret = secret;
        this.type = AuthenticationType.TOTP;
    }

    public Application(String label, String secret, AuthenticationType type) {
        this.label = label;
        this.secret = secret;
        this.type = type;
    }
}
