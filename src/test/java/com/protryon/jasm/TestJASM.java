package com.protryon.jasm;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.common.base.Charsets;
import com.protryon.jasm.decompiler.ControlFlowGraph;
import com.protryon.jasm.decompiler.Decompiler;
import com.protryon.jasm.decompiler.DecompilerReducer;
import com.protryon.jasm.decompiler.StackEntry;
import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.StackDirector;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.ImmutableList;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJASM {

    private int tempCounter = 0;

    @Test
    public void testJASM() throws IOException {

        // this is all local testing stuff for Minecraft that cannot legally be pushed to GitHub AFAIK

        // if you wish to run this test, download minecraft and modify this as you please

        // i will probably write better tests for this in the future......

        Classpath classpath = new Classpath("/p/git/Burger/1.14.jar");

        File outDir = new File("./out");
        outDir.mkdirs();

        for (Klass klass : classpath.getKlasses().values()) {
            try {
                CompilationUnit decompiled = Decompiler.decompileClass(classpath, klass);
                Files.write(outDir.toPath().resolve(klass.name + ".java"), decompiled.toString().getBytes(Charsets.UTF_8));
            } catch (Exception e) {
                System.err.println("failed to decompile " + klass.name + ":");
                e.printStackTrace();
            }
        }
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
