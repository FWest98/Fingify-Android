package com.fwest98.fingify.Helpers.Apache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

class StringEntity extends AbstractHttpEntity implements Cloneable {
    protected final byte[] content;

    public StringEntity(String s, String charset)
            throws UnsupportedEncodingException {
        if (s == null) {
            throw new IllegalArgumentException("Source string may not be null");
        }
        if (charset == null) {
            charset = "ISO-8859-1";
        }
        content = s.getBytes(charset);
        setContentType("text/plain; charset=" + charset);
    }

    public long getContentLength() {
        return content.length;
    }

    public InputStream getContent() throws IOException {
        return new ByteArrayInputStream(content);
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        outstream.write(content);
        outstream.flush();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
