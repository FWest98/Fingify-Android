package com.fwest98.fingify.Helpers.Apache;

interface FormattedHeader extends Header {
    /**
     * Obtains the buffer with the formatted header.
     * The returned buffer MUST NOT be modified.
     *
     * @return  the formatted header, in a buffer that must not be modified
     */
    CharArrayBuffer getBuffer()
    ;

}
