package com.protryon.jasm.util

import java.io.ByteArrayInputStream

class TrackingByteArrayInputStream : ByteArrayInputStream {

    val position: Int
        get() = this.pos

    constructor(buf: ByteArray) : super(buf)

    constructor(buf: ByteArray, offset: Int, length: Int) : super(buf, offset, length)
}
