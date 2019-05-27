package com.protryon.jasm

import com.protryon.jasm.Helper.compileKlass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestJASM {

    @Test
    fun testJASM() {
        val klass = compileKlass("test", "public class test {}")
        assertEquals(1, klass.methods.size)
        assertEquals("<init>", klass.methods[0].name)
        assertEquals(0, klass.fields.size)
    }

}