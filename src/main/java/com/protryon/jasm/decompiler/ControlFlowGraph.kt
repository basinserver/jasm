package com.protryon.jasm.decompiler

import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.NullLiteralExpr
import com.github.javaparser.ast.visitor.GenericVisitor
import com.github.javaparser.ast.visitor.VoidVisitor
import com.protryon.jasm.JType
import com.protryon.jasm.Method
import com.protryon.jasm.instruction.Instruction
import com.protryon.jasm.instruction.instructions.*
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.Pair
import com.shapesecurity.functional.data.ImmutableList

import java.util.Arrays
import java.util.HashMap
import java.util.LinkedList

class ControlFlowGraph(val method: Method) {
    val nodes: LinkedList<Node>
    val labelMap = HashMap<Label, Node>()

    init {
        this.nodes = LinkedList()
        this.create()
    }

    abstract inner class NodeEnd {

        abstract fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>>

        abstract override fun toString(): String

    }

    inner class NodeEndJump(var target: Label) : NodeEnd() {

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.of(Pair.of<Node, ImmutableList<StackEntry<Expression>>>(labelMap[this.target], stack))
        }

        override fun toString(): String {
            return "goto " + target.name
        }
    }

    inner class NodeEndFallthrough(var fallthrough: Node) : NodeEnd() {

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.of(Pair.of(this.fallthrough, stack))
        }

        override fun toString(): String {
            return "fall"
        }
    }

    inner class NodeEndBranch(var branch: Instruction, var fallthrough: Node, var target: Label) : NodeEnd() {

        var invertedMemoCondition: StackEntry<Expression>? = null

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            val pair = conditionCreators[branch.javaClass]!!.invoke(stack)
            this.invertedMemoCondition = pair.left
            val modifiedStack = pair.right
            return ImmutableList.of(
                    Pair.of(this.fallthrough, modifiedStack),
                    Pair.of(labelMap[this.target], modifiedStack)
            ) as ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>>
        }

        override fun toString(): String {
            return invertedMemoCondition!!.value.toString() + ": " + branch.toString()
        }
    }

    inner class NodeEndThrow : NodeEnd() {

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.empty()
        }

        override fun toString(): String {
            return "throw"
        }
    }

    inner class NodeEndEnterTry(exceptionType: JType, catchBlock: Label, fallthrough: Node) : NodeEnd() {

        var exceptionType: JType? = null
        var catchBlock: Label? = null
        var fallthrough: Node? = null

        init {
            this.exceptionType = exceptionType
            this.catchBlock = catchBlock
            this.fallthrough = fallthrough
        }

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.of(Pair.of<Node, ImmutableList<StackEntry<Expression>>>(this.fallthrough, stack), Pair.of<Node, ImmutableList<StackEntry<Expression>>>(labelMap[this.catchBlock], ImmutableList.of(StackEntry<Expression>(exceptionType!!, NullLiteralExpr()))))
        }

        override fun toString(): String {
            return "enter_try " + exceptionType!!.toString()
        }
    }

    inner class NodeEndExitTry(catchBlock: Label, fallthrough: Node) : NodeEnd() {

        var catchBlock: Label? = null
        var fallthrough: Node? = null

        init {
            this.catchBlock = catchBlock
            this.fallthrough = fallthrough
        }

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.of(Pair.of<Node, ImmutableList<StackEntry<Expression>>>(this.fallthrough, stack))
        }

        override fun toString(): String {
            return "exit_try"
        }
    }

    inner class NodeEndMonitorEnter(var fallthrough: Node) : NodeEnd() {

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.of(Pair.of(fallthrough, stack.maybeTail().fromJust()))
        }

        override fun toString(): String {
            return "monitor-enter-fall"
        }
    }

    inner class NodeEndMonitorExit(var fallthrough: Node) : NodeEnd() {

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.of(Pair.of(fallthrough, stack.maybeTail().fromJust()))
        }

        override fun toString(): String {
            return "monitor-exit-fall"
        }
    }

    inner class NodeEndReturn(var ret: Instruction) : NodeEnd() {

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.empty()
        }

        override fun toString(): String {
            return ret.toString()
        }
    }

    inner class NodeEndLookupswitch(var instruction: Lookupswitch) : NodeEnd() {
        var switchOn: StackEntry<Expression>? = null

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            this.switchOn = stack.maybeHead().fromJust()
            return Arrays.stream(this.instruction.pairs!!).collect(ImmutableList.collector()).map { pair -> Pair.of<Node, ImmutableList<StackEntry<Expression>>>(labelMap[pair.right], stack.maybeTail().fromJust()) }
                    .cons(Pair.of(labelMap[instruction.default], stack.maybeTail().fromJust()))
        }

        override fun toString(): String {
            return instruction.toString()
        }
    }

    inner class NodeEndTableswitch(var instruction: Tableswitch) : NodeEnd() {
        var switchOn: StackEntry<Expression>? = null

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            this.switchOn = stack.maybeHead().fromJust()
            return Arrays.stream(this.instruction.offsets!!).collect(ImmutableList.collector()).map { label -> Pair.of<Node, ImmutableList<StackEntry<Expression>>>(labelMap[label], stack.maybeTail().fromJust()) }
                    .cons(Pair.of(labelMap[instruction.default], stack.maybeTail().fromJust()))
        }

        override fun toString(): String {
            return instruction.toString()
        }
    }

    inner class ExpressionReturnHack(val node: Node) : Expression() {
        override fun <R : Any?, A : Any?> accept(v: GenericVisitor<R, A>?, arg: A): R {
            error("undefined")
        }

        override fun <A : Any?> accept(v: VoidVisitor<A>?, arg: A) {
            error("undefined")
        }

    }

    inner class NodeEndCallSub(var subroutine: Label, var fallthrough: Node) : NodeEnd() {

        // ugly hacks to get the node through the stack
        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            return ImmutableList.of(Pair.of<Node, ImmutableList<StackEntry<Expression>>>(labelMap[subroutine], stack.cons(StackEntry(JType.voidT, ExpressionReturnHack(fallthrough)))))
        }

        override fun toString(): String {
            return "callsub " + subroutine.name
        }
    }

    inner class NodeEndRetSub : NodeEnd() {

        override fun applyToStack(stack: ImmutableList<StackEntry<Expression>>): ImmutableList<Pair<Node, ImmutableList<StackEntry<Expression>>>> {
            val node = (stack.maybeHead().fromJust().value as ExpressionReturnHack).node
            return ImmutableList.of(Pair.of(node, stack.maybeTail().fromJust()))
        }

        override fun toString(): String {
            return "retsub"
        }
    }

    inner class Node {
        var instructions: LinkedList<Instruction> = LinkedList()
        var end: NodeEnd? = null

    }

    private fun create() {
        var currentBlock: Node? = Node()
        for (instruction in this.method.code) {
            if (instruction.isControl || isReturnInstruction(instruction) || isMonitorInstruction(instruction) || instruction is Athrow) {
                if (instruction is Athrow || isReturnInstruction(instruction)) {
                    currentBlock!!.instructions.add(instruction)
                }

                val nextBlock = Node()
                when {
                    instruction is Athrow -> currentBlock!!.end = NodeEndThrow()
                    instruction is Monitorenter -> currentBlock!!.end = NodeEndMonitorEnter(nextBlock)
                    instruction is Monitorexit -> currentBlock!!.end = NodeEndMonitorExit(nextBlock)
                    isReturnInstruction(instruction) -> currentBlock!!.end = NodeEndReturn(instruction)
                    instruction is Jsr -> currentBlock!!.end = NodeEndCallSub(instruction.branchbyte!!, nextBlock)
                    instruction is Jsr_w -> currentBlock!!.end = NodeEndCallSub(instruction.branchbyte!!, nextBlock)
                    instruction is Ret -> currentBlock!!.end = NodeEndRetSub()
                    instruction is Goto -> currentBlock!!.end = NodeEndJump(instruction.branchbyte!!)
                    instruction is Goto_w -> currentBlock!!.end = NodeEndJump(instruction.branchbyte!!)
                    instruction is Lookupswitch -> currentBlock!!.end = NodeEndLookupswitch(instruction)
                    instruction is Tableswitch -> currentBlock!!.end = NodeEndTableswitch(instruction)
                    instruction is If_acmpeq -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is If_acmpne -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is If_icmpeq -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is If_icmpge -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is If_icmpgt -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is If_icmple -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is If_icmplt -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is If_icmpne -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Ifeq -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Ifge -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Ifgt -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Ifle -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Iflt -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Ifne -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Ifnonnull -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is Ifnull -> currentBlock!!.end = NodeEndBranch(instruction, nextBlock, instruction.branchbyte!!)
                    instruction is EnterTry -> currentBlock!!.end = NodeEndEnterTry(instruction.exceptionType, instruction.catchBlock, nextBlock)
                    instruction is ExitTry -> currentBlock!!.end = NodeEndExitTry(instruction.catchBlock!!, nextBlock)
                    instruction is Label -> {
                        if (currentBlock!!.instructions.size == 0) {
                            currentBlock = null
                        } else {
                            currentBlock.end = NodeEndFallthrough(nextBlock)
                        }
                        labelMap[instruction] = nextBlock
                    }
                    else -> throw RuntimeException("not reached: " + instruction.javaClass.simpleName)
                }
                if (currentBlock != null) {
                    this.nodes.add(currentBlock)
                }
                currentBlock = nextBlock
            } else {
                currentBlock!!.instructions.add(instruction)
            }
        }
        if (currentBlock!!.instructions.size > 0) {
            this.nodes.add(currentBlock)
        }
    }

    companion object {

        private val conditionCreators = HashMap<Class<out Instruction>, (ImmutableList<StackEntry<Expression>>)->Pair<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>>()

        init {
            // inverted for if statement conditions
            conditionCreators[If_acmpeq::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.NOT_EQUALS)), modifiedStack)
            }
            conditionCreators[If_acmpne::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.EQUALS)), modifiedStack)
            }

            conditionCreators[If_icmpeq::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.NOT_EQUALS)), modifiedStack)
            }
            conditionCreators[If_icmpne::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.EQUALS)), modifiedStack)
            }
            conditionCreators[If_icmplt::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.GREATER_EQUALS)), modifiedStack)
            }
            conditionCreators[If_icmpge::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LESS)), modifiedStack)
            }
            conditionCreators[If_icmpgt::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LESS_EQUALS)), modifiedStack)
            }
            conditionCreators[If_icmple::class.java] = { stack ->
                val value1 = stack.maybeHead().fromJust()
                val value2 = stack.index(1).fromJust()
                val modifiedStack = stack.drop(2)
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.GREATER)), modifiedStack)
            }

            conditionCreators[Ifeq::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, IntegerLiteralExpr(0), BinaryExpr.Operator.NOT_EQUALS)), modifiedStack)
            }
            conditionCreators[Ifne::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, IntegerLiteralExpr(0), BinaryExpr.Operator.EQUALS)), modifiedStack)
            }
            conditionCreators[Iflt::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, IntegerLiteralExpr(0), BinaryExpr.Operator.GREATER_EQUALS)), modifiedStack)
            }
            conditionCreators[Ifge::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, IntegerLiteralExpr(0), BinaryExpr.Operator.LESS)), modifiedStack)
            }
            conditionCreators[Ifgt::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, IntegerLiteralExpr(0), BinaryExpr.Operator.LESS_EQUALS)), modifiedStack)
            }
            conditionCreators[Ifle::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, IntegerLiteralExpr(0), BinaryExpr.Operator.GREATER)), modifiedStack)
            }
            conditionCreators[Ifnonnull::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, NullLiteralExpr(), BinaryExpr.Operator.EQUALS)), modifiedStack)
            }
            conditionCreators[Ifnull::class.java] = { stack ->
                val head = stack.maybeHead().fromJust()
                val modifiedStack = stack.maybeTail().fromJust()
                Pair.of<StackEntry<Expression>, ImmutableList<StackEntry<Expression>>>(StackEntry(JType.booleanT, BinaryExpr(head.value, NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS)), modifiedStack)
            }
        }

        fun isReturnInstruction(instruction: Instruction): Boolean {
            return instruction is Areturn ||
                    instruction is Dreturn ||
                    instruction is Freturn ||
                    instruction is Ireturn ||
                    instruction is Lreturn ||
                    instruction is Return
        }

        fun isMonitorInstruction(instruction: Instruction): Boolean {
            return instruction is Monitorenter || instruction is Monitorexit
        }
    }

}
