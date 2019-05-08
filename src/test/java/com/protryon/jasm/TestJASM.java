package com.protryon.jasm;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.protryon.jasm.decompiler.ControlFlowGraph;
import com.protryon.jasm.decompiler.DecompilerReducer;
import com.protryon.jasm.decompiler.StackEntry;
import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.StackDirector;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;

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

        Klass bmm = classpath.lookupKlass("bmm");

        Method clinit = bmm.methods.get(1);

        System.out.println(clinit.toString());

        ControlFlowGraph g = new ControlFlowGraph(clinit);


        LinkedList<StackEntry<Expression>> stack = new LinkedList<>();
        for (ControlFlowGraph.Node n : g.nodes) {
            DecompilerReducer reducer = new DecompilerReducer(clinit, statement -> {
                System.out.println(statement.toString());
            }, () -> tempCounter++);

            /*if (n.instructions.size() > 0) {
                Instruction last = n.instructions.getLast();
                if (last.isControl()) {
                    n.instructions.removeLast();
                }
            }*/
            stack = StackDirector.reduceInstructions(reducer, n.instructions, stack);
        }
        assertEquals(0, stack.size());


        assertNotNull(bmm);

        //System.out.println(bmm);

    }
}
