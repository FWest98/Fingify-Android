package com.fwest98.fingify.Helpers.Apache;

class CharArrayBuffer {
    private char[] buffer;
    private int len;

    public CharArrayBuffer(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Buffer capacity may not be negative");
        }
        buffer = new char[capacity];
    }

    private void expand(int newlen) {
        char newbuffer[] = new char[Math.max(buffer.length << 1, newlen)];
        System.arraycopy(buffer, 0, newbuffer, 0, len);
        buffer = newbuffer;
    }

    public void append(String str) {
        if (str == null) {
            str = "null";
        }
        int strlen = str.length();
        int newlen = len + strlen;
        if (newlen > buffer.length) {
            expand(newlen);
        }
        str.getChars(0, strlen, buffer, len);
        len = newlen;
    }

    public void clear() {
        len = 0;
    }

    public int length() {
        return len;
    }

    public void ensureCapacity(int required) {
        int available = buffer.length - len;
        if (required > available) {
            expand(len + required);
        }
    }

    public void setLength(int len) {
        if (len < 0 || len > buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        this.len = len;
    }

    public String toString() {
        return new String(buffer, 0, len);
    }
}
