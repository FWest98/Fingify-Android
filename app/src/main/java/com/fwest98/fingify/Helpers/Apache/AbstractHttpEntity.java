package com.fwest98.fingify.Helpers.Apache;

class AbstractHttpEntity {
    protected Header contentType;


    /**
     * Protected default constructor.
     * The attributes of the created object remain
     * <code>null</code> and <code>false</code>, respectively.
     */
    protected AbstractHttpEntity() {
    }


    /**
     * Specifies the Content-Type header.
     * The default implementation sets the value of the
     * {@link #contentType contentType} attribute.
     *
     * @param contentType the new Content-Encoding header, or
     *                    <code>null</code> to unset
     */
    public void setContentType(Header contentType) {
        this.contentType = contentType;
    }

    /**
     * Specifies the Content-Type header, as a string.
     * The default implementation calls
     * {@link #setContentType(Header) setContentType(Header)}.
     *
     * @param ctString the new Content-Type header, or
     *                 <code>null</code> to unset
     */
    public void setContentType(String ctString) {
        Header h = null;
        if (ctString != null) {
            h = new BasicHeader("Content-Type", ctString);
        }
        setContentType(h);
    }
}
