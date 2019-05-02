package com.protryon.jasm;

import com.protryon.jasm.decompiler.DecompilerReducer;
import com.protryon.jasm.instruction.StackDirector;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJASM {

    @Test
    public void testJASM() throws IOException {

        // this is all local testing stuff for Minecraft that cannot legally be pushed to GitHub AFAIK

        // if you wish to run this test, download minecraft and modify this as you please

        // i will probably write better tests for this in the future......

        Classpath classpath = new Classpath("/p/git/Burger/1.14.jar");

        Klass bmm = classpath.lookupKlass("bmm");

        Method clinit = bmm.methods.get(1);

        System.out.println(clinit.toString());

        assertEquals(0, StackDirector.reduceInstructions(new DecompilerReducer(System.out::println), clinit.code, new LinkedList<>()).size());

        assertNotNull(bmm);

        //System.out.println(bmm);

    }
}
