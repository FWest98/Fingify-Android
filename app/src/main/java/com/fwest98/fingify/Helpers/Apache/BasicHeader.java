package com.fwest98.fingify.Helpers.Apache;

class BasicHeader implements Header, Cloneable {
    /**
     * Header name.
     */
    private final String name;

    /**
     * Header value.
     */
    private final String value;

    /**
     * Constructor with name and value
     *
     * @param name the header name
     * @param value the header value
     */
    public BasicHeader(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the header name.
     *
     * @return String name The name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the header value.
     *
     * @return String value The current value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a {@link String} representation of the header.
     *
     * @return a string
     */
    public String toString() {
        // no need for non-default formatting in toString()
        return BasicLineFormatter.DEFAULT.formatHeader(null, this).toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
