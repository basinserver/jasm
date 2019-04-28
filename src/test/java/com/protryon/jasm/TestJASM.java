package com.protryon.jasm;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJASM {

    @Test
    public void testJASM() throws IOException {

        // this is all local testing stuff for Minecraft that cannot legally be pushed to GitHub AFAIK

        // if you wish to run this test, download minecraft and modify this as you please

        // i will probably write better tests for this in the future......

        Classpath classpath = new Classpath("/p/git/Burger/1.14.jar");

        Klass bmm = classpath.lookupKlass("bmm");

        assertNotNull(bmm);

        System.out.println(bmm);

    }
}
