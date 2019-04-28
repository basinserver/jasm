package com.protryon.jasm.util;

import java.io.ByteArrayInputStream;

public class TrackingByteArrayInputStream extends ByteArrayInputStream {
    public TrackingByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    public TrackingByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    public int getPosition() {
        return this.pos;
    }
}
