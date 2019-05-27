package com.protryon.jasm.decompiler

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.type.*
import com.protryon.jasm.*
import com.protryon.jasm.instruction.StackDirector
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.F
import com.shapesecurity.functional.Pair
import com.shapesecurity.functional.Tuple3
import com.shapesecurity.functional.data.ImmutableList
import com.shapesecurity.functional.data.Maybe

import java.util.*
import java.util.stream.Collector
import java.util.stream.Collectors

class Decompiler private constructor(private val graph: ControlFlowGraph) {

    private val basicBlocks = HashMap<ControlFlowGraph.Node, List<Statement>>()

    private val memoEmission = HashMap<ControlFlowGraph.Node, ImmutableList<ControlFlowGraph.Node>>()
    // maybe.empty = nop immediate maybe.of(false) = return immediately maybe.of(true) = append basic block and return
    private var earlyTermination: ((ControlFlowGraph.Node)->Maybe<Boolean>)? = null

    //TODO: probably: node duplication for return short circuiting with try/catches
    private fun emitNode(node: ControlFlowGraph.Node, outStatements: MutableList<Statement>): ImmutableList<ControlFlowGraph.Node> {
        if (memoEmission.containsKey(node)) {
            outStatements.add(ExpressionStmt(StringLiteralExpr("memoized?")))
            // this might not be right!
            return ImmutableList.empty() // in progress
        }
        if (earlyTermination != null) {
            val termination = earlyTermination!!.invoke(node)
            if (termination.isJust) {
                if (termination.fromJust()) {
                    outStatements.addAll(this.basicBlocks[node]!!)
                    return ImmutableList.of(node)
                } else {
                    return ImmutableList.empty()
                }
            }
        }
        memoEmission[node] = ImmutableList.empty<ControlFlowGraph.Node>()
        val emitted = ArrayList<ControlFlowGraph.Node>()
        emitted.add(node)
        if (node.end is ControlFlowGraph.NodeEndJump) {
            //TODO: disconnected flow
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
            // outStatements.add(new ExpressionStmt(new StringLiteralExpr("goto x")));
        } else if (node.end is ControlFlowGraph.NodeEndFallthrough) {
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
            emitted.addAll(emitNode((node.end as ControlFlowGraph.NodeEndFallthrough).fallthrough, outStatements).toArrayList())
        } else if (node.end is ControlFlowGraph.NodeEndEnterTry) {
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
            // var lastEarlyTermination = this.earlyTermination;
            val catchBlockLabel = (node.end as ControlFlowGraph.NodeEndEnterTry).catchBlock

            val exceptionalBlock = ArrayList<Statement>()
            var startAt: ControlFlowGraph.Node? = (node.end as ControlFlowGraph.NodeEndEnterTry).fallthrough

            val catchBlock = graph.labelMap[catchBlockLabel]!!

            while (startAt != null) {
                val exceptionalEmitted = emitNode(startAt, exceptionalBlock).toArrayList()
                startAt = null
                emitted.addAll(exceptionalEmitted)
                var exitTryNode: ControlFlowGraph.Node? = null
                for (i in exceptionalEmitted.indices.reversed()) {
                    val exceptionalNode = exceptionalEmitted[i]
                    if (exceptionalNode.end is ControlFlowGraph.NodeEndExitTry) {
                        exitTryNode = exceptionalNode
                    }
                }
                if (exitTryNode == null) {
                    throw RuntimeException("try-block termination node not found")
                }

                val postEndNode = (exitTryNode.end as ControlFlowGraph.NodeEndExitTry).fallthrough!!
                when (postEndNode.end) {
                    is ControlFlowGraph.NodeEndJump -> {
                        val end = postEndNode.end as ControlFlowGraph.NodeEndJump
                        val postStatements = ArrayList<Statement>()
                        emitted.addAll(emitNode(graph.labelMap[end.target]!!, postStatements).toArrayList())
                        val catchStatements = ArrayList<Statement>()
                        emitted.addAll(emitNode(catchBlock, catchStatements).toArrayList())
                        outStatements.add(TryStmt(BlockStmt(NodeList(exceptionalBlock)), NodeList(CatchClause(NodeList(), NodeList(), Decompiler.convertType((node.end as ControlFlowGraph.NodeEndEnterTry).exceptionType!!) as ClassOrInterfaceType, SimpleName("e"), BlockStmt(NodeList(catchStatements)))), BlockStmt(NodeList())))
                        outStatements.addAll(postStatements)
                    }
                    is ControlFlowGraph.NodeEndReturn -> {
                        error("node end return")
                    }
                    is ControlFlowGraph.NodeEndExitTry -> {
                        error("node end exit try")
                    }
                    else -> error("assert failed: ${postEndNode.end?.javaClass?.simpleName}")
                }
                emitted.addAll(emitNode(postEndNode, outStatements).toArrayList())
                /*ControlFlowGraph.Node postCatchJumpBlock = exitTryNode;
                while (postCatchJumpBlock.end instanceof ControlFlowGraph.NodeEndExitTry) {
                    postCatchJumpBlock = ((ControlFlowGraph.NodeEndExitTry) postCatchJumpBlock.end).fallthrough;
                    emitted.add(postCatchJumpBlock);
                }
                if (postCatchJumpBlock.end instanceof ControlFlowGraph.NodeEndJump) {
                    if (((ControlFlowGraph.NodeEndExitTry) exitTryNode.end).catchBlock != catchBlockLabel) {
                        throw new RuntimeException("broken exception stack");
                    }
                    NodeList<Statement> catchBlock = new NodeList<>();
                    emitted.addAll(emitNode(postCatchJumpBlock, exceptionalBlock).toArrayList());
                    Label postCatchBlockLabel = ((ControlFlowGraph.NodeEndJump) postCatchJumpBlock.end).target;
                    lastTerminateAt = this.terminateAt;
                    this.terminateAt = postCatchBlockLabel;
                    emitted.addAll(emitNode(graph.labelMap.get(catchBlockLabel), catchBlock).toArrayList());
                    this.terminateAt = lastTerminateAt;
                    emitted.addAll(emitNode(graph.labelMap.get(postCatchBlockLabel), outStatements).toArrayList());
                    outStatements.add(new TryStmt(new BlockStmt(new NodeList<>(exceptionalBlock)), new NodeList<>(new CatchClause(new NodeList<>(), new NodeList<>(), (ClassOrInterfaceType) Decompiler.convertType(((ControlFlowGraph.NodeEndEnterTry) node.end).exceptionType), new SimpleName("e"), new BlockStmt(catchBlock))), new BlockStmt(new NodeList<>())));
                } else if (postCatchJumpBlock.end instanceof ControlFlowGraph.NodeEndReturn || postCatchJumpBlock.end == null) {
                    emitted.addAll(emitNode(postCatchJumpBlock, exceptionalBlock).toArrayList());
                } else {
                    // finally block

                    throw new RuntimeException("unexpected try-block post-termination node end type: " + postCatchJumpBlock.end.getClass().getSimpleName());
                }*/
            }

            // this.terminateAt = lastTerminateAt;
            //TODO: proper expression naming/stack resolution?
        } else if (node.end is ControlFlowGraph.NodeEndExitTry) {
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
            // emitted.addAll(emitNode((node.end as ControlFlowGraph.NodeEndExitTry).fallthrough!!, outStatements).toArrayList())
            return ImmutableList.of(node)
        } else if (node.end is ControlFlowGraph.NodeEndBranch) {
            outStatements.addAll(this.basicBlocks[node]!!)
            val branch = node.end as ControlFlowGraph.NodeEndBranch
            val targetBlock = ArrayList<Statement>()
            val targetNode = graph.labelMap[branch.target]!!
            val targetEmission = emitNode(targetNode, targetBlock)
            val fallthroughBlock = ArrayList<Statement>()
            val fallEmission = emitNode(branch.fallthrough, fallthroughBlock)
            var isLoop = false
            val block = BlockStmt(NodeList(fallthroughBlock))
            val fallthroughTerminator = fallEmission.maybeLast().orJustLazy { memoEmission[branch.fallthrough]!!.maybeLast().fromJust() }
            if (fallthroughTerminator.end is ControlFlowGraph.NodeEndJump) {
                val jumpTarget = graph.labelMap[(fallthroughTerminator.end as ControlFlowGraph.NodeEndJump).target]
                if (jumpTarget === node) {
                    outStatements.add(WhileStmt(branch.invertedMemoCondition!!.value, block))
                    isLoop = true
                } else {
                    // TODO: elses and other?
                    // throw new RuntimeException("not reached");
                }
            }
            if (!isLoop) {
                outStatements.add(IfStmt(branch.invertedMemoCondition!!.value, block, null))
            }
            emitted.addAll(fallEmission.toArrayList())
            emitted.add(node)
            outStatements.addAll(targetBlock)
            emitted.addAll(targetEmission.toArrayList())
        } else if (node.end is ControlFlowGraph.NodeEndThrow) {
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
        } else if (node.end is ControlFlowGraph.NodeEndMonitorEnter) {
            throw RuntimeException("monitorenter")
        } else if (node.end is ControlFlowGraph.NodeEndMonitorExit) {
            throw RuntimeException("monitorexit")
        } else if (node.end is ControlFlowGraph.NodeEndReturn) {
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
        } else if (node.end is ControlFlowGraph.NodeEndLookupswitch) {
            throw RuntimeException("lookupswitch")
        } else if (node.end is ControlFlowGraph.NodeEndTableswitch) {
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
            val tableswitch = node.end as ControlFlowGraph.NodeEndTableswitch
            val switchEntries = NodeList<SwitchEntry>()
            val cases = Arrays.stream(tableswitch.instruction.offsets!!).map { graph.labelMap[it] }.collect(Collectors.toSet())
            cases.add(graph.labelMap[tableswitch.instruction.default])
            val lastEarlyTermination = this.earlyTermination
            this.earlyTermination = { subNode -> if (cases.contains(subNode)) Maybe.of(false) else Maybe.empty() }

            this.earlyTermination = lastEarlyTermination
            outStatements.add(SwitchStmt(tableswitch.switchOn!!.value, switchEntries))

        } else if (node.end is ControlFlowGraph.NodeEndCallSub) {
            throw RuntimeException("callsub")
        } else if (node.end is ControlFlowGraph.NodeEndRetSub) {
            throw RuntimeException("retsub")
        } else if (node.end == null) {
            emitted.add(node)
            outStatements.addAll(this.basicBlocks[node]!!)
        } else {
            throw RuntimeException("not reached")
        }
        val emittedList = ImmutableList.from(emitted)
        memoEmission[node] = emittedList
        return emittedList
    }

    companion object {

        fun decompileClass(classpath: Classpath, klass: Klass): CompilationUnit {
            val modifiers = NodeList<Modifier>()
            if (klass.isPublic) {
                modifiers.add(Modifier(Modifier.Keyword.PUBLIC))
            }
            if (klass.isAbstract) {
                modifiers.add(Modifier(Modifier.Keyword.ABSTRACT))
            }
            if (klass.isFinal) {
                modifiers.add(Modifier(Modifier.Keyword.FINAL))
            }
            val extendingKlass = klass.extending
            val extending = NodeList<ClassOrInterfaceType>()
            if (extendingKlass != null && extendingKlass.name != "java/lang/Object") {
                extending.add(convertType(JType.instance(extendingKlass)) as ClassOrInterfaceType)
            }
            val implementing = klass.interfaces.stream().map { iface -> convertType(JType.instance(iface)) as ClassOrInterfaceType }.collect<NodeList<ClassOrInterfaceType>, Any>(Collectors.toCollection<ClassOrInterfaceType, NodeList<ClassOrInterfaceType>> { NodeList() } as Collector<ClassOrInterfaceType, Any, NodeList<ClassOrInterfaceType>>)
            val bodyDeclarations = NodeList<BodyDeclaration<*>>()
            for (field in klass.fields.values) {
                val fieldModifiers = NodeList<Modifier>()
                if (field.isPublic) {
                    fieldModifiers.add(Modifier(Modifier.Keyword.PUBLIC))
                }
                if (field.isProtected) {
                    fieldModifiers.add(Modifier(Modifier.Keyword.PROTECTED))
                }
                if (field.isPrivate) {
                    fieldModifiers.add(Modifier(Modifier.Keyword.PRIVATE))
                }
                if (field.isStatic) {
                    fieldModifiers.add(Modifier(Modifier.Keyword.STATIC))
                }
                if (field.isFinal) {
                    fieldModifiers.add(Modifier(Modifier.Keyword.FINAL))
                }
                if (field.isVolatile) {
                    fieldModifiers.add(Modifier(Modifier.Keyword.VOLATILE))
                }
                // TODO: inline clinit definitions for fields here
                bodyDeclarations.add(FieldDeclaration(fieldModifiers, VariableDeclarator(convertType(field.type), field.name)))
            }
            for (method in klass.methods) {
                bodyDeclarations.add(decompileMethod(classpath, method))
            }
            val lastSlashIndex = klass.name.lastIndexOf("/")
            val packageName = klass.name.substring(0, if (lastSlashIndex < 0) 0 else lastSlashIndex).replace("/", ".")
            val compilationUnit = if (packageName.length == 0) CompilationUnit() else CompilationUnit(packageName)
            val className = klass.name.substring(lastSlashIndex + 1)
            compilationUnit.addType(ClassOrInterfaceDeclaration(modifiers, NodeList(), klass.isInterface, SimpleName(className), NodeList(), extending, implementing, bodyDeclarations))
            return compilationUnit
        }

        fun decompileMethod(classpath: Classpath, method: Method): MethodDeclaration {

            val g = ControlFlowGraph(method)

            // no nodes *should* be processed with more than one stack
            val unclobberedMemo = HashSet<ControlFlowGraph.Node>()
            val memo = HashSet<Pair<ControlFlowGraph.Node, ImmutableList<StackEntry<Expression>>>>()

            val pendingNodes = Stack<Tuple3<ControlFlowGraph.Node, ImmutableList<StackEntry<Expression>>, LocalContext>>()
            val decompiler = Decompiler(g)
            val rootContext = LocalContext()
            var argumentIndex = 0
            if (!method.isStatic) {
                rootContext.getOrMakeLocal(argumentIndex++, JType.instance(method.parent))
            }
            for (argumentType in method.descriptor.parameters) {
                rootContext.getOrMakeLocal(argumentIndex++, argumentType)
                if (argumentType.computationType == 2) {
                    argumentIndex++
                }
            }

            if (g.nodes.size > 0) {
                pendingNodes.push(Tuple3.of(g.nodes.first, ImmutableList.empty(), rootContext.fork()))
            }
            while (!pendingNodes.empty()) {
                val tuple = pendingNodes.pop()
                val pairOfTuple = Pair.of(tuple.a, tuple.b)
                if (memo.contains(pairOfTuple)) {
                    continue
                }
                if (unclobberedMemo.contains(tuple.a)) {
                    // not illegal necessarily but bad!
                    // throw new RuntimeException("WARNING: clobbered stack state (unbalanced loop)");
                }
                unclobberedMemo.add(tuple.a)
                memo.add(pairOfTuple)
                val outStatements = ArrayList<Statement>()
                // System.out.println(statement.toString());
                val reducer = DecompilerReducer(classpath, method, tuple.c) {
                    outStatements.add(it)
                }
                val stack = StackDirector.reduceInstructions(reducer, tuple.a.instructions, tuple.b) {
                    it.type.computationType == 2
                }
                decompiler.basicBlocks[tuple.a] = outStatements
                if (tuple.a.end != null) {
                    tuple.a.end!!.applyToStack(stack).forEach { pair -> pendingNodes.push(Tuple3.of(pair.left, pair.right, tuple.c.fork())) }
                }
            }
            val outStatements = ArrayList<Statement>()
            if (g.nodes.size > 0) {
                decompiler.emitNode(g.nodes.first, outStatements)
            }
            val modifiers = ArrayList<Modifier>()
            if (method.isPublic) {
                modifiers.add(Modifier(Modifier.Keyword.PUBLIC))
            }
            if (method.isProtected) {
                modifiers.add(Modifier(Modifier.Keyword.PROTECTED))
            }
            if (method.isPrivate) {
                modifiers.add(Modifier(Modifier.Keyword.PRIVATE))
            }
            if (method.isStatic) {
                modifiers.add(Modifier(Modifier.Keyword.STATIC))
            }
            if (method.isFinal) {
                modifiers.add(Modifier(Modifier.Keyword.FINAL))
            }
            if (method.isSynchronized) {
                modifiers.add(Modifier(Modifier.Keyword.SYNCHRONIZED))
            }
            if (method.isAbstract) {
                modifiers.add(Modifier(Modifier.Keyword.ABSTRACT))
            }
            val argumentCounter = intArrayOf(0)
            if (!method.isStatic) {
                argumentCounter[0]++
            }
            val parameters = method.descriptor.parameters.stream().map { type ->
                val parameter = Parameter(convertType(type), SimpleName("v" + argumentCounter[0]++))
                if (type.computationType == 2) {
                    argumentCounter[0]++
                }
                parameter
            }.collect<List<Parameter>, Any>(Collectors.toList<Parameter>() as Collector<in Parameter, Any, List<Parameter>>?)
            val methodBlock = BlockStmt(NodeList(outStatements))
            return MethodDeclaration(NodeList(modifiers), NodeList(), NodeList(), convertType(method.descriptor.returnType), SimpleName(method.name), NodeList(parameters), NodeList(), methodBlock)
        }

        internal fun convertType(type: JType): Type {
            return when {
                type === JType.voidT -> VoidType()
                type === JType.byteT -> PrimitiveType.byteType()
                type === JType.charT -> PrimitiveType.charType()
                type === JType.shortT -> PrimitiveType.shortType()
                type === JType.intT -> PrimitiveType.intType()
                type === JType.longT -> PrimitiveType.longType()
                type === JType.floatT -> PrimitiveType.floatType()
                type === JType.doubleT -> PrimitiveType.doubleType()
                type === JType.booleanT -> PrimitiveType.booleanType()
                type === JType.nullT -> ClassOrInterfaceType("Object")
                type is JType.JTypeArray -> ArrayType(convertType(type.elementType))
                type is JType.JTypeInstance -> ClassOrInterfaceType(type.klass.name.replace("/", "."))
                else -> throw UnsupportedOperationException("Unknown type for conversion: " + type.niceName)
            }
        }
    }
}
