package com.fwest98.fingify.Models;

import com.j256.ormlite.field.DatabaseField;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import lombok.Getter;

public class Request implements Serializable {
    private static final long serialVersionUID = 7865671;

    @DatabaseField @Getter private UUID id;

    @DatabaseField @Getter private String applicationName;
    @DatabaseField @Getter private Date requestTime;
    @DatabaseField @Getter private boolean answered;
    @DatabaseField @Getter private boolean thisDevice;
    @DatabaseField @Getter private boolean accepted;
    @DatabaseField @Getter private String ipAddress;
    @DatabaseField @Getter private String deviceName;
    @DatabaseField @Getter private Platform platform;

    public Request() {}

    public Request(UUID id, String applicationName, Date requestTime) {
        this.applicationName = applicationName;
        this.requestTime = requestTime;
        this.answered = false;
        this.thisDevice = true;
        this.accepted = false;
    }

    public Request(UUID id, String applicationName, Date requestTime, boolean answered, boolean thisDevice, boolean accepted, String ipAddress, String deviceName, Platform platform) {
        this.applicationName = applicationName;
        this.requestTime = requestTime;
        this.answered = answered;
        this.thisDevice = thisDevice;
        this.accepted = accepted;
        this.ipAddress = ipAddress;
        this.deviceName = deviceName;
        this.platform = platform;
    }

    public Request(JSONObject jsonRequest) throws JSONException {
        SimpleDateFormat webFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date requestTime;

        try {
            requestTime = webFormat.parse(jsonRequest.getString("requestTime"));
        } catch(ParseException e) {
            requestTime = Calendar.getInstance().getTime();
        }

        this.id = UUID.fromString(jsonRequest.getString("requestId"));
        this.applicationName = jsonRequest.getString("applicationName");
        this.requestTime = requestTime;
        this.answered = jsonRequest.getBoolean("isDone");
        this.thisDevice = jsonRequest.getBoolean("isLocal");
        this.accepted = jsonRequest.getBoolean("isAccepted");
        this.ipAddress = jsonRequest.getString("ipAddress");
        this.deviceName = jsonRequest.getString("deviceName");
        this.platform = Platform.valueOf(jsonRequest.getString("platform").toUpperCase());
    }

    public enum Platform {
        ANDROID, IOS, WEB
    }
}
