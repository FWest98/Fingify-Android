package com.fwest98.fingify.Helpers;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fwest98.fingify.Settings.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class ApiRequest<T> extends Request<T> {
    @Setter @Getter private Response.Listener<T> listener;
    private final ResponseParseCallbacks<T> dataParser;
    public boolean hasNewApiVersion = false;

    public ApiRequest(int method, String url, ResponseParseCallbacks<T> dataParser, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.dataParser = dataParser;

        this.setRetryPolicy(new DefaultRetryPolicy(Constants.TIMEOUT_MILLIS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public ApiRequest(String url, ResponseParseCallbacks<T> dataParser, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, dataParser, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("APIVersion", Constants.API_VERSION);

        return baseHeaders;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return new HashMap<>();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String entryValue = entry.getValue();
                if(entryValue == null) entryValue = "";
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entryValue, paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        // Check API Version
        hasNewApiVersion = response.headers.containsKey("CurrentAPIVersion");

        String dataString;
        try {
            dataString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            dataString = new String(response.data);
        }

        try {
            return Response.success(dataParser.parseData(dataString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    public interface ResponseParseCallbacks<T> {
        T parseData(String repsonse) throws Exception;
    }
}
