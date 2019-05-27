package com.protryon.jasm

import com.protryon.jasm.Helper.compileMethod
import com.protryon.jasm.decompiler.Decompiler
import org.junit.jupiter.api.Test

class TestTryCatchFinally {

    @Test
    fun testTryCatch() {
        val method = compileMethod("test", "try {System.out.println(\"try\");} catch (Exception e) {System.out.println(\"catch\");}System.out.println(\"post\");")
        System.out.println(method.toString())
        val decompiled = Decompiler.decompileMethod(Classpath(linkedMapOf()), method)
        System.out.println(decompiled)
    }

    @Test
    fun testTryCatchFinally() {
        val method = compileMethod("test", "try {System.out.println(\"try\");} catch (Exception e) {System.out.println(\"catch\");} finally {System.out.println(\"finally\");}")
        System.out.println(method.toString())
        val decompiled = Decompiler.decompileMethod(Classpath(linkedMapOf()), method)
        System.out.println(decompiled)
    }

}