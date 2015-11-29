package com.fwest98.fingify.Helpers.Apache;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class URLEncodedUtils {
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";

    /**
     * Returns a String that is suitable for use as an <code>application/x-www-form-urlencoded</code>
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param encoding The encoding to use.
     */
    public static String format (
            final List<? extends NameValuePair> parameters,
            final String encoding) {
        final StringBuilder result = new StringBuilder();
        for (final NameValuePair parameter : parameters) {
            final String encodedName = encode(parameter.getName(), encoding);
            final String value = parameter.getValue();
            final String encodedValue = value != null ? encode(value, encoding) : "";
            if (result.length() > 0)
                result.append(PARAMETER_SEPARATOR);
            result.append(encodedName);
            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }
        return result.toString();
    }

    private static String encode (final String content, final String encoding) {
        try {
            return URLEncoder.encode(content,
                    encoding != null ? encoding : "ISO-8859-1");
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }
}
