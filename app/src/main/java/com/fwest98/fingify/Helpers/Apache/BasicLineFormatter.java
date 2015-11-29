package com.fwest98.fingify.Helpers.Apache;

class BasicLineFormatter {
    public final static BasicLineFormatter DEFAULT = new BasicLineFormatter();



    // public default constructor


    /**
     * Obtains a buffer for formatting.
     *
     * @param buffer    a buffer already available, or <code>null</code>
     *
     * @return  the cleared argument buffer if there is one, or
     *          a new empty buffer that can be used for formatting
     */
    protected CharArrayBuffer initBuffer(CharArrayBuffer buffer) {
        if (buffer != null) {
            buffer.clear();
        } else {
            buffer = new CharArrayBuffer(64);
        }
        return buffer;
    }


    // non-javadoc, see interface LineFormatter
    public CharArrayBuffer formatHeader(CharArrayBuffer buffer,
                                        Header header) {
        if (header == null) {
            throw new IllegalArgumentException
                    ("Header may not be null");
        }
        CharArrayBuffer result;

        if (header instanceof FormattedHeader) {
            // If the header is backed by a buffer, re-use the buffer
            result = ((FormattedHeader)header).getBuffer();
        } else {
            result = initBuffer(buffer);
            doFormatHeader(result, header);
        }
        return result;

    } // formatHeader


    /**
     * Actually formats a header.
     * Called from {@link #formatHeader}.
     *
     * @param buffer    the empty buffer into which to format,
     *                  never <code>null</code>
     * @param header    the header to format, never <code>null</code>
     */
    protected void doFormatHeader(CharArrayBuffer buffer,
                                  Header header) {
        String name = header.getName();
        String value = header.getValue();

        int len = name.length() + 2;
        if (value != null) {
            len += value.length();
        }
        buffer.ensureCapacity(len);

        buffer.append(name);
        buffer.append(": ");
        if (value != null) {
            buffer.append(value);
        }
    }

}
