package com.protryon.jasm.decompiler;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.protryon.jasm.JType;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.F;
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

    private static final HashMap<Class<? extends Instruction>, F<ImmutableList<StackEntry<Expression>>, Pair<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>>> conditionCreators = new HashMap<>();

    static {
        // inverted for if statement conditions
        conditionCreators.put(If_acmpeq.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.NOT_EQUALS)), stack);
        });
        conditionCreators.put(If_acmpne.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.EQUALS)), stack);
        });

        conditionCreators.put(If_icmpeq.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.NOT_EQUALS)), stack);
        });
        conditionCreators.put(If_icmpne.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.EQUALS)), stack);
        });
        conditionCreators.put(If_icmplt.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.GREATER_EQUALS)), stack);
        });
        conditionCreators.put(If_icmpge.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LESS)), stack);
        });
        conditionCreators.put(If_icmpgt.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LESS_EQUALS)), stack);
        });
        conditionCreators.put(If_icmple.class, stack -> {
            var value1 = stack.maybeHead().fromJust();
            var value2 = stack.index(1).fromJust();
            stack = stack.drop(2);
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.GREATER)), stack);
        });

        conditionCreators.put(Ifeq.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new IntegerLiteralExpr(0), BinaryExpr.Operator.NOT_EQUALS)), stack);
        });
        conditionCreators.put(Ifne.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new IntegerLiteralExpr(0), BinaryExpr.Operator.EQUALS)), stack);
        });
        conditionCreators.put(Iflt.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new IntegerLiteralExpr(0), BinaryExpr.Operator.GREATER_EQUALS)), stack);
        });
        conditionCreators.put(Ifge.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new IntegerLiteralExpr(0), BinaryExpr.Operator.LESS)), stack);
        });
        conditionCreators.put(Ifgt.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new IntegerLiteralExpr(0), BinaryExpr.Operator.LESS_EQUALS)), stack);
        });
        conditionCreators.put(Ifle.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new IntegerLiteralExpr(0), BinaryExpr.Operator.GREATER)), stack);
        });
        conditionCreators.put(Ifnonnull.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new NullLiteralExpr(), BinaryExpr.Operator.EQUALS)), stack);
        });
        conditionCreators.put(Ifnull.class, stack -> {
            var head = stack.maybeHead().fromJust();
            stack = stack.maybeTail().fromJust();
            return Pair.of(new StackEntry<>(JType.booleanT, new BinaryExpr(head.value, new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS)), stack);
        });
    }

    public abstract class NodeEnd {

        public abstract ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack);

        @Override
        public abstract String toString();

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

        @Override
        public String toString() {
            return "goto " + target.name;
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

        @Override
        public String toString() {
            return "fall";
        }
    }

    public class NodeEndBranch extends NodeEnd {

        public StackEntry<Expression> invertedMemoCondition = null;
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
            var pair = conditionCreators.get(branch.getClass()).apply(stack);
            this.invertedMemoCondition = pair.left;
            stack = pair.right;
            return ImmutableList.of(
                Pair.of(this.fallthrough, stack),
                Pair.of(labelMap.get(this.target), stack)
            );
        }

        @Override
        public String toString() {
            return invertedMemoCondition.value.toString() + ": " + branch.toString();
        }
    }

    public class NodeEndThrow extends NodeEnd {

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.empty();
        }

        @Override
        public String toString() {
            return "throw";
        }
    }

    public class NodeEndEnterTry extends NodeEnd {

        public JType exceptionType = null;
        public Label catchBlock = null;
        public Node fallthrough = null;

        public NodeEndEnterTry(JType exceptionType, Label catchBlock, Node fallthrough) {
            this.exceptionType = exceptionType;
            this.catchBlock = catchBlock;
            this.fallthrough = fallthrough;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(Pair.of(this.fallthrough, stack), Pair.of(labelMap.get(this.catchBlock), ImmutableList.of(new StackEntry<>(exceptionType, new NullLiteralExpr()))));
        }

        @Override
        public String toString() {
            return "enter_try " + exceptionType.toString();
        }
    }

    public class NodeEndExitTry extends NodeEnd {

        public Label catchBlock = null;
        public Node fallthrough = null;

        public NodeEndExitTry(Label catchBlock, Node fallthrough) {
            this.catchBlock = catchBlock;
            this.fallthrough = fallthrough;
        }

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            return ImmutableList.of(Pair.of(this.fallthrough, stack));
        }

        @Override
        public String toString() {
            return "exit_try";
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

        @Override
        public String toString() {
            return "monitor-enter-fall";
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

        @Override
        public String toString() {
            return "monitor-exit-fall";
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

        @Override
        public String toString() {
            return ret.toString();
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

        @Override
        public String toString() {
            return instruction.toString();
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

        @Override
        public String toString() {
            return instruction.toString();
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

        @Override
        public String toString() {
            return "callsub " + subroutine.name;
        }
    }

    public class NodeEndRetSub extends NodeEnd {

        @Override
        public ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> applyToStack(ImmutableList<StackEntry<Expression>> stack) {
            Node node = (Node) ((ImmutableList) stack).maybeHead().fromJust();
            return ImmutableList.of(Pair.of(node, stack.maybeInit().fromJust()));
        }

        @Override
        public String toString() {
            return "retsub";
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
            instruction instanceof Lreturn ||
            instruction instanceof Return;
    }

    public static boolean isMonitorInstruction(Instruction instruction) {
        return instruction instanceof Monitorenter ||
            instruction instanceof Monitorexit;
    }

    private void create() {
        Node currentBlock = new Node();
        for (Instruction instruction : this.method.code) {
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
                } else if (instruction instanceof If_icmpgt) {
                    currentBlock.end = new NodeEndBranch(instruction, nextBlock, ((If_icmpgt) instruction).branchbyte);
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
                } else if (instruction instanceof EnterTry) {
                    EnterTry enterTry = (EnterTry) instruction;
                    currentBlock.end = new NodeEndEnterTry(enterTry.exceptionType, enterTry.catchBlock, nextBlock);
                } else if (instruction instanceof ExitTry) {
                    ExitTry exitTry = (ExitTry) instruction;
                    currentBlock.end = new NodeEndExitTry(exitTry.catchBlock, nextBlock);
                } else if (instruction instanceof Label) {
                    if (currentBlock.instructions.size() == 0) {
                        currentBlock = null;
                    } else {
                        currentBlock.end = new NodeEndFallthrough(nextBlock);
                    }
                    labelMap.put((Label) instruction, nextBlock);
                } else {
                    throw new RuntimeException("not reached: " + instruction.getClass().getSimpleName());
                }
                if (currentBlock != null) {
                    this.nodes.add(currentBlock);
                }
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
