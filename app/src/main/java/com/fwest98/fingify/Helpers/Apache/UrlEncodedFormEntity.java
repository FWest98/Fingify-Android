package com.fwest98.fingify.Helpers.Apache;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class UrlEncodedFormEntity extends StringEntity {
    public UrlEncodedFormEntity (
            final List <? extends NameValuePair> parameters) throws UnsupportedEncodingException {
        super(URLEncodedUtils.format(parameters, "ISO-8859-1"),
                "ISO-8859-1");
        setContentType(URLEncodedUtils.CONTENT_TYPE);
    }
}
