package com.protryon.jasm

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.utils.CodeGenerationUtils
import com.google.common.base.Charsets
import com.protryon.jasm.decompiler.ControlFlowGraph
import com.protryon.jasm.decompiler.Decompiler
import com.protryon.jasm.decompiler.DecompilerReducer
import com.protryon.jasm.decompiler.StackEntry
import com.protryon.jasm.instruction.Instruction
import com.protryon.jasm.instruction.StackDirector
import com.shapesecurity.functional.Pair
import com.shapesecurity.functional.data.ImmutableList
import org.junit.jupiter.api.Test

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

class TestJASM {

    @Test
    @Throws(IOException::class)
    fun testJASM() {

        // this is all local testing stuff for Minecraft that cannot legally be pushed to GitHub AFAIK

        // if you wish to run this test, download minecraft and modify this as you please

        // i will probably write better tests for this in the future......

        val classpath = Classpath(arrayOf("/home/p/.minecraft/libraries"), arrayOf("/p/git/Burger/1.14.jar"))

        val outDir = File("./out")
        outDir.mkdirs()
        var completed = 0
        var total = 0

        for (klass in classpath.getKlasses().values) {
            // if (!klass.name.equals("bej")) continue;
            ++total
            try {
                val decompiled = Decompiler.decompileClass(classpath, klass)
                val javaPath = outDir.toPath().resolve(klass.name + ".java")
                javaPath.parent.toFile().mkdirs()
                Files.write(javaPath, decompiled.toString().toByteArray(Charsets.UTF_8))
                ++completed
            } catch (e: Exception) {
                System.err.println("failed to decompile " + klass.name + ":")
                e.printStackTrace()
            }

        }
        println("completed $completed / $total classes")
        /*
        Klass bmm = classpath.lookupKlass("bmm");

        assertNotNull(bmm);
        Method clinit = bmm.methods.get(1);
        assertNotNull(clinit);

        // System.out.println(clinit.toString());

        // MethodDeclaration decompiled = Decompiler.decompileMethod(clinit);
        CompilationUnit decompiled = Decompiler.decompileClass(bmm);

        System.out.println(decompiled);
*/

        //System.out.println(bmm);

    }
}
