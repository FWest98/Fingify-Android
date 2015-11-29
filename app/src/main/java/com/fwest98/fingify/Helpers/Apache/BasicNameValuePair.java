package com.fwest98.fingify.Helpers.Apache;

public class BasicNameValuePair implements NameValuePair, Cloneable {
    private final String name;
    private final String value;

    /**
     * Default Constructor taking a name and a value. The value may be null.
     *
     * @param name The name.
     * @param value The value.
     */
    public BasicNameValuePair(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name.
     *
     * @return String name The name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     *
     * @return String value The current value.
     */
    public String getValue() {
        return value;
    }


    /**
     * Get a string representation of this pair.
     *
     * @return A string representation.
     */
    public String toString() {
        // don't call complex default formatting for a simple toString

        int len = name.length();
        if (value != null)
            len += 1 + value.length();
        CharArrayBuffer buffer = new CharArrayBuffer(len);

        buffer.append(name);
        if (value != null) {
            buffer.append("=");
            buffer.append(value);
        }
        return buffer.toString();
    }

    public boolean equals(Object object) {
        if (object == null) return false;
        if (this == object) return true;
        if (object instanceof NameValuePair) {
            BasicNameValuePair that = (BasicNameValuePair) object;
            return name.equals(that.name)
                    && LangUtils.equals(value, that.value);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, name);
        hash = LangUtils.hashCode(hash, value);
        return hash;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
