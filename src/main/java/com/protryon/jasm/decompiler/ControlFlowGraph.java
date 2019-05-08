package com.protryon.jasm.decompiler;

import com.github.javaparser.ast.expr.Expression;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.ImmutableList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class ControlFlowGraph {

    public final Method method;
    public final LinkedList<Node> nodes;
    public final HashMap<Label, Node> labelMap = new HashMap<>();

    public ControlFlowGraph(Method method) {
        this.method = method;
        this.nodes = new LinkedList<>();
        this.create();
    }

    public abstract class NodeEnd {

        public abstract ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack);

    }

    public class NodeEndJump extends NodeEnd {

        public Label target;

        public NodeEndJump(Label target) {
            this.target = target;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(Pair.of(labelMap.get(this.target), stack));
        }
    }

    public class NodeEndFallthrough extends NodeEnd {

        public Node fallthrough;

        public NodeEndFallthrough(Node fallthrough) {
            this.fallthrough = fallthrough;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(Pair.of(this.fallthrough, stack));
        }
    }

    public class NodeEndBranch extends NodeEnd {

        public Instruction branch;
        public Node fallthrough;
        public Label target;

        public NodeEndBranch(Instruction branch, Node fallthrough, Label target) {
            this.branch = branch;
            this.fallthrough = fallthrough;
            this.target = target;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(
                Pair.of(this.fallthrough, stack.maybeInit().fromJust()),
                Pair.of(labelMap.get(this.target), stack.maybeInit().fromJust())
            );
        }
    }

    public class NodeEndThrow extends NodeEnd {

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            //TODO
        }
    }

    public class NodeEndMonitorEnter extends NodeEnd {

        public Node fallthrough;

        public NodeEndMonitorEnter(Node fallthrough) {
            this.fallthrough = fallthrough;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(Pair.of(fallthrough, stack.maybeInit().fromJust()));
        }
    }

    public class NodeEndMonitorExit extends NodeEnd {

        public Node fallthrough;

        public NodeEndMonitorExit(Node fallthrough) {
            this.fallthrough = fallthrough;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(Pair.of(fallthrough, stack.maybeInit().fromJust()));
        }
    }

    public class NodeEndReturn extends NodeEnd {

        public Instruction ret;

        public NodeEndReturn(Instruction ret) {
            this.ret = ret;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.empty();
        }
    }

    public class NodeEndLookupswitch extends NodeEnd {

        public Lookupswitch instruction;

        public NodeEndLookupswitch(Lookupswitch instruction) {
            this.instruction = instruction;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return Arrays.stream(this.instruction.pairs).collect(ImmutableList.collector()).map(pair -> Pair.of(labelMap.get(pair.right), stack.maybeInit().fromJust()))
                .cons(Pair.of(labelMap.get(instruction._default), stack.maybeInit().fromJust()));
        }
    }

    public class NodeEndTableswitch extends NodeEnd {

        public Tableswitch instruction;

        public NodeEndTableswitch(Tableswitch instruction) {
            this.instruction = instruction;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return Arrays.stream(this.instruction.offsets).collect(ImmutableList.collector()).map(label -> Pair.of(labelMap.get(label), stack.maybeInit().fromJust()))
                .cons(Pair.of(labelMap.get(instruction._default), stack.maybeInit().fromJust()));
        }
    }

    public class NodeEndCallSub extends NodeEnd {

        public Label subroutine;
        public Node fallthrough;

        public NodeEndCallSub(Label subroutine, Node fallthrough) {
            this.subroutine = subroutine;
            this.fallthrough = fallthrough;
        }

        // ugly hacks to get the node through the stack
        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(Pair.of(labelMap.get(subroutine), ((ImmutableList) stack).cons(fallthrough)));
        }
    }

    public class NodeEndRetSub extends NodeEnd {

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            Node node = (Node) ((ImmutableList) stack).maybeHead().fromJust();
            return ImmutableList.of(Pair.of(node, stack.maybeInit().fromJust()));
        }
    }

    public class Node {
        public LinkedList<Instruction> instructions;
        public NodeEnd end;

        public Node() {
            this.instructions = new LinkedList<>();
            this.end = null;
        }
    }

    public static boolean isReturnInstruction(Instruction instruction) {
        return instruction instanceof Areturn ||
            instruction instanceof Dreturn ||
            instruction instanceof Freturn ||
            instruction instanceof Ireturn ||
            instruction instanceof Lreturn;
    }

    public static boolean isMonitorInstruction(Instruction instruction) {
        return instruction instanceof Monitorenter ||
            instruction instanceof Monitorexit;
    }

    private void create() {
        Node currentBlock = new Node();
        for (Instruction instruction : method.code) {
            if (instruction.isControl() || isReturnInstruction(instruction) || isMonitorInstruction(instruction) || instruction instanceof Athrow) {
                if (instruction instanceof Athrow || isReturnInstruction(instruction)) {
                    currentBlock.instructions.add(instruction);
                }

                Node nextBlock = new Node();
                if (instruction instanceof Athrow) {
                    currentBlock.end = new NodeEndThrow();
                } else if (instruction instanceof Monitorenter) {
                    currentBlock.end = new NodeEndMonitorEnter(nextBlock);
                } else if (instruction instanceof Monitorexit) {
                    currentBlock.end = new NodeEndMonitorExit(nextBlock);
                } else if (isReturnInstruction(instruction)) {
                    currentBlock.end = new NodeEndReturn(instruction);
                } else if (instruction instanceof Jsr) {
                    currentBlock.end = new NodeEndCallSub(((Jsr) instruction).branchbyte, nextBlock);
                } else if (instruction instanceof Jsr_w) {
                    currentBlock.end = new NodeEndCallSub(((Jsr_w) instruction).branchbyte, nextBlock);
                } else if (instruction instanceof Ret) {
                    currentBlock.end = new NodeEndRetSub();
                } else if (instruction instanceof Goto) {
                    currentBlock.end = new NodeEndJump(((Goto) instruction).branchbyte);
                } else if (instruction instanceof Goto_w) {
                    currentBlock.end = new NodeEndJump(((Goto_w) instruction).branchbyte);
                } else if (instruction instanceof Lookupswitch) {
                    currentBlock.end = new NodeEndLookupswitch((Lookupswitch) instruction);
                } else if (instruction instanceof Tableswitch) {
                    currentBlock.end = new NodeEndTableswitch((Tableswitch) instruction);
                } else if (instruction instanceof If_acmpeq) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_acmpeq) instruction).branchbyte);
                } else if (instruction instanceof If_acmpne) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_acmpne) instruction).branchbyte);
                } else if (instruction instanceof If_icmpeq) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_icmpeq) instruction).branchbyte);
                } else if (instruction instanceof If_icmpge) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_icmpge) instruction).branchbyte);
                } else if (instruction instanceof If_icmple) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_icmple) instruction).branchbyte);
                } else if (instruction instanceof If_icmplt) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_icmplt) instruction).branchbyte);
                } else if (instruction instanceof If_icmpne) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_icmpne) instruction).branchbyte);
                } else if (instruction instanceof Ifeq) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Ifeq) instruction).branchbyte);
                } else if (instruction instanceof Ifge) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Ifge) instruction).branchbyte);
                } else if (instruction instanceof Ifgt) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Ifgt) instruction).branchbyte);
                } else if (instruction instanceof Ifle) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Ifle) instruction).branchbyte);
                } else if (instruction instanceof Iflt) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Iflt) instruction).branchbyte);
                } else if (instruction instanceof Ifne) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Ifne) instruction).branchbyte);
                } else if (instruction instanceof Ifnonnull) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Ifnonnull) instruction).branchbyte);
                } else if (instruction instanceof Ifnull) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((Ifnull) instruction).branchbyte);
                } else if (instruction instanceof Label) {
                    currentBlock.end = new NodeEndFallthrough(nextBlock);
                    labelMap.put((Label) instruction, nextBlock);
                } else { // branch
                    throw new RuntimeException("not reached: " + instruction.getClass().getSimpleName());
                }
                this.nodes.add(currentBlock);
                currentBlock = nextBlock;
            } else {
                currentBlock.instructions.add(instruction);
            }
        }
        if (currentBlock.instructions.size() > 0) {
            this.nodes.add(currentBlock);
        }
    }

}
