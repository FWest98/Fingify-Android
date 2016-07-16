package com.fwest98.fingify.Helpers;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fwest98.fingify.Data.AccountManager;
import com.fwest98.fingify.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.SneakyThrows;

public class HmacApiRequest<T> extends ApiRequest<T> {
    private Context context;
    private String privateKey;
    private String publicKey;

    public HmacApiRequest(int method, String url, ResponseParseCallbacks<T> dataParser, Response.Listener<T> listener, Response.ErrorListener errorListener, Context context) {
        super(method, url, dataParser, listener, errorListener);
        this.context = context;
        this.privateKey = AccountManager.getPrivateKey();
        this.publicKey = AccountManager.getPublicKey();
    }

    public HmacApiRequest(int method, String url, ResponseParseCallbacks<T> dataParser, Response.Listener<T> listener, Response.ErrorListener errorListener, Context context, String privateKey, String publicKey) {
        this(method, url, dataParser, listener, errorListener, context);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    @SneakyThrows(UnsupportedEncodingException.class) // UTF-8 MUST always be present in any JVM implementation
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> baseHeaders = super.getHeaders();

        String requestUri, requestContentHashBase64 = "", requestHttpMethod = getMethodName();
        requestUri = URLEncoder.encode(this.getUrl(), "utf-8").toLowerCase();

        // Calculate UNIX time
        Long unixTime = System.currentTimeMillis() / 1000L;
        String requestTimeStamp = unixTime.toString();

        String nonce = UUID.randomUUID().toString().replace("-", "");

        byte[] contentBytes = getBody();
        if(contentBytes != null) {
            try {
                MessageDigest md5Digest = MessageDigest.getInstance("MD5");
                md5Digest.update(contentBytes);
                byte[] requestContentHashBytes = md5Digest.digest();
                requestContentHashBase64 = Base64.encodeToString(requestContentHashBytes, Base64.NO_WRAP);
            } catch(NoSuchAlgorithmException e) {
                throw new AuthFailureError(context.getString(R.string.apirequests_systemerror), e);
            }
        }

        AccountManager.initialize(context, false);
        if(!AccountManager.isSet()) return baseHeaders; // can't encrypt

        String signature = publicKey + requestHttpMethod + requestUri + requestTimeStamp + nonce + requestContentHashBase64;
        try {
            byte[] secretKeyBytes = privateKey.getBytes("utf-8");
            byte[] signatureBytes = signature.getBytes("utf-8");

            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            hmac.init(secretKey);
            byte[] signatureHashBytes = hmac.doFinal(signatureBytes);
            String signatureHashBase64 = Base64.encodeToString(signatureHashBytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);

            baseHeaders.put("Authorization", "amx " + publicKey + ":" + signatureHashBase64 + ":" + nonce + ":" + requestTimeStamp);
        } catch(InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AuthFailureError(context.getString(R.string.apirequests_systemerror), e);
        }

        return baseHeaders;
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        if(volleyError.networkResponse == null) return volleyError;
        switch (volleyError.networkResponse.statusCode) {
            case 401:
                return new VolleyError(context.getString(R.string.apirequests_unauthorized), volleyError);
            case 500:
                return new VolleyError(context.getString(R.string.apirequests_servererror), volleyError);
        }

        return super.parseNetworkError(volleyError);
    }

    private String getMethodName() {
        switch(getMethod()) {
            case Method.GET: return "GET";
            case Method.DELETE: return "DELETE";
            case Method.HEAD: return "HEAD";
            case Method.OPTIONS: return "OPTIONS";
            case Method.PATCH: return "PATCH";
            case Method.POST: return "POST";
            case Method.PUT: return "PUT";
            case Method.TRACE: return "TRACE";
        }
        return null;
    }
}
