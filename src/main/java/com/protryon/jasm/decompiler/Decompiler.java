package com.protryon.jasm.decompiler;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.protryon.jasm.*;
import com.protryon.jasm.instruction.StackDirector;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;

public final class Decompiler {

    private final Map<ControlFlowGraph.Node, List<Statement>> basicBlocks = new HashMap<>();
    private final ControlFlowGraph graph;

    private Decompiler(ControlFlowGraph graph) {
        this.graph = graph;
    }

    public static CompilationUnit decompileClass(Classpath classpath, Klass klass) {
        NodeList<Modifier> modifiers = new NodeList<>();
        if (klass.isPublic) {
            modifiers.add(new Modifier(Modifier.Keyword.PUBLIC));
        }
        if (klass.isAbstract) {
            modifiers.add(new Modifier(Modifier.Keyword.ABSTRACT));
        }
        if (klass.isFinal) {
            modifiers.add(new Modifier(Modifier.Keyword.FINAL));
        }
        Klass extendingKlass = klass.extending;
        NodeList<ClassOrInterfaceType> extending = new NodeList<>();
        if (extendingKlass != null && !extendingKlass.name.equals("java/lang/Object")) {
            extending.add((ClassOrInterfaceType) convertType(JType.instance(klass.extending)));
        }
        NodeList<ClassOrInterfaceType> implementing = klass.interfaces.stream().map(iface -> (ClassOrInterfaceType) convertType(JType.instance(iface))).collect(Collectors.toCollection(NodeList::new));
        NodeList<BodyDeclaration<?>> bodyDeclarations = new NodeList<>();
        for (Field field : klass.fields.values()) {
            NodeList<Modifier> fieldModifiers = new NodeList<>();
            if (field.isPublic) {
                fieldModifiers.add(new Modifier(Modifier.Keyword.PUBLIC));
            }
            if (field.isProtected) {
                fieldModifiers.add(new Modifier(Modifier.Keyword.PROTECTED));
            }
            if (field.isPrivate) {
                fieldModifiers.add(new Modifier(Modifier.Keyword.PRIVATE));
            }
            if (field.isStatic) {
                fieldModifiers.add(new Modifier(Modifier.Keyword.STATIC));
            }
            if (field.isFinal) {
                fieldModifiers.add(new Modifier(Modifier.Keyword.FINAL));
            }
            if (field.isVolatile) {
                fieldModifiers.add(new Modifier(Modifier.Keyword.VOLATILE));
            }
            // TODO: inline clinit definitions for fields here
            bodyDeclarations.add(new FieldDeclaration(fieldModifiers, new VariableDeclarator(convertType(field.type), field.name)));
        }
        for (Method method : klass.methods) {
            bodyDeclarations.add(decompileMethod(classpath, method));
        }
        int lastSlashIndex = klass.name.lastIndexOf("/");
        String packageName = klass.name.substring(0, lastSlashIndex < 0 ? 0 : lastSlashIndex).replace("/", ".");
        CompilationUnit compilationUnit = packageName.length() == 0 ? new CompilationUnit() : new CompilationUnit(packageName);
        String className = klass.name.substring(lastSlashIndex + 1);
        compilationUnit.addType(new ClassOrInterfaceDeclaration(modifiers, new NodeList<>(), klass.isInterface, new SimpleName(className), new NodeList<>(), extending, implementing, bodyDeclarations));
        return compilationUnit;
    }

    public static MethodDeclaration decompileMethod(Classpath classpath, Method method) {
        int[] tempCounter = new int[]{0}; // java hack

        ControlFlowGraph g = new ControlFlowGraph(method);

        // no nodes *should* be processed with more than one stack
        Set<ControlFlowGraph.Node> unclobberedMemo = new HashSet<>();
        Set<Pair<ControlFlowGraph.Node, ImmutableList<StackEntry<Expression>>>> memo = new HashSet<>();

        Stack<Pair<ControlFlowGraph.Node, ImmutableList<StackEntry<Expression>>>> pendingNodes = new Stack<>();
        Decompiler decompiler = new Decompiler(g);

        int argumentIndex = 0;
        if (!method.isStatic) {
            method.getOrMakeLocal(argumentIndex++).setOrAssertType(JType.instance(method.parent));
        }
        for (JType argumentType : method.descriptor.parameters) {
            method.getOrMakeLocal(argumentIndex++).setOrAssertType(argumentType);
            if (argumentType.computationType == 2) {
                argumentIndex++;
            }
        }

        if (g.nodes.size() > 0) {
            pendingNodes.push(Pair.of(g.nodes.getFirst(), ImmutableList.empty()));
        }
        while (!pendingNodes.empty()) {
            var pair = pendingNodes.pop();
            if (memo.contains(pair)) {
                continue;
            }
            if (pair.left == null) {
                System.out.println("TODO: exceptions");
                continue;
            }
            if (unclobberedMemo.contains(pair.left)) {
                // not illegal necessarily but bad!
                throw new RuntimeException("WARNING: clobbered stack state (unbalanced loop)");
            }
            unclobberedMemo.add(pair.left);
            memo.add(pair);
            List<Statement> outStatements = new ArrayList<>();
            // System.out.println(statement.toString());
            DecompilerReducer reducer = new DecompilerReducer(classpath, method, outStatements::add, () -> tempCounter[0]++);
            decompiler.basicBlocks.put(pair.left, outStatements);
            var stack = StackDirector.reduceInstructions(reducer, pair.left.instructions, pair.right);
            if (pair.left.end != null) {
                pair.left.end.applyToStack(stack).forEach(pendingNodes::add);
            }
        }
        List<Statement> outStatements = new ArrayList<>();
        if (g.nodes.size() > 0) {
            decompiler.emitNode(g.nodes.getFirst(), outStatements);
        }
        List<Modifier> modifiers = new ArrayList<>();
        if (method.isPublic) {
            modifiers.add(new Modifier(Modifier.Keyword.PUBLIC));
        }
        if (method.isProtected) {
            modifiers.add(new Modifier(Modifier.Keyword.PROTECTED));
        }
        if (method.isPrivate) {
            modifiers.add(new Modifier(Modifier.Keyword.PRIVATE));
        }
        if (method.isStatic) {
            modifiers.add(new Modifier(Modifier.Keyword.STATIC));
        }
        if (method.isFinal) {
            modifiers.add(new Modifier(Modifier.Keyword.FINAL));
        }
        if (method.isSynchronized) {
            modifiers.add(new Modifier(Modifier.Keyword.SYNCHRONIZED));
        }
        if (method.isAbstract) {
            modifiers.add(new Modifier(Modifier.Keyword.ABSTRACT));
        }
        int[] argumentCounter = new int[]{0};
        if (!method.isStatic) {
            argumentCounter[0]++;
        }
        List<Parameter> parameters = method.descriptor.parameters.stream().map(type -> {
            Parameter parameter = new Parameter(convertType(type), new SimpleName("v" + argumentCounter[0]++));
            if (type.computationType == 2) {
                argumentCounter[0]++;
            }
            return parameter;
        }).collect(Collectors.toList());
        BlockStmt methodBlock = new BlockStmt(new NodeList<>(outStatements));
        return new MethodDeclaration(new NodeList<>(modifiers), new NodeList<>(), new NodeList<>(), convertType(method.descriptor.returnType), new SimpleName(method.name), new NodeList<>(parameters), new NodeList<>(), methodBlock);
    }

    static Type convertType(JType type) {
        if (type == JType.voidT) {
            return new VoidType();
        } else if (type == JType.byteT) {
            return PrimitiveType.byteType();
        } else if (type == JType.charT) {
            return PrimitiveType.charType();
        } else if (type == JType.shortT) {
            return PrimitiveType.shortType();
        } else if (type == JType.intT) {
            return PrimitiveType.intType();
        } else if (type == JType.longT) {
            return PrimitiveType.longType();
        } else if (type == JType.floatT) {
            return PrimitiveType.floatType();
        } else if (type == JType.doubleT) {
            return PrimitiveType.doubleType();
        } else if (type == JType.booleanT) {
            return PrimitiveType.booleanType();
        } else if (type == JType.nullT) {
            return new ClassOrInterfaceType("Object");
        } else if (type instanceof JType.JTypeArray) {
            return new ArrayType(convertType(((JType.JTypeArray) type).elementType));
        } else if (type instanceof JType.JTypeInstance) {
            return new ClassOrInterfaceType(((JType.JTypeInstance) type).klass.name.replace("/", "."));
        } else {
            throw new UnsupportedOperationException("Unknown type for conversion: " + type.niceName);
        }
    }

    private final Set<ControlFlowGraph.Node> emitted = new HashSet<>();
    private final HashMap<ControlFlowGraph.Node, ImmutableList<ControlFlowGraph.Node>> memoEmission = new HashMap<>();

    private ImmutableList<ControlFlowGraph.Node> emitNode(ControlFlowGraph.Node node, List<Statement> outStatements) {
        if (emitted.contains(node)) {
            return ImmutableList.empty(); // in progress
        }
        List<ControlFlowGraph.Node> emitted = new ArrayList<>();
        emitted.add(node);
        if (node.end instanceof ControlFlowGraph.NodeEndJump) {
            //TODO: disconnected flow
            emitted.add(node);
            outStatements.addAll(this.basicBlocks.get(node));
            // outStatements.add(new ExpressionStmt(new StringLiteralExpr("goto x")));
        } else if (node.end instanceof ControlFlowGraph.NodeEndFallthrough) {
            emitted.add(node);
            outStatements.addAll(this.basicBlocks.get(node));
            emitted.addAll(emitNode(((ControlFlowGraph.NodeEndFallthrough) node.end).fallthrough, outStatements).toArrayList());
        } else if (node.end instanceof ControlFlowGraph.NodeEndBranch) {
            outStatements.addAll(this.basicBlocks.get(node));
            ControlFlowGraph.NodeEndBranch branch = (ControlFlowGraph.NodeEndBranch) node.end;
            List<Statement> targetBlock = new ArrayList<>();
            ControlFlowGraph.Node targetNode = graph.labelMap.get(branch.target);
            ImmutableList<ControlFlowGraph.Node> targetEmission = emitNode(targetNode, targetBlock);
            List<Statement> fallthroughBlock = new ArrayList<>();
            ImmutableList<ControlFlowGraph.Node> fallEmission = emitNode(branch.fallthrough, fallthroughBlock);
            boolean isLoop = false;
            BlockStmt block = new BlockStmt(new NodeList<>(fallthroughBlock));
            ControlFlowGraph.Node fallthroughTerminator = memoEmission.get(branch.fallthrough).maybeLast().fromJust();
            if (fallthroughTerminator.end instanceof ControlFlowGraph.NodeEndJump) {
                ControlFlowGraph.Node jumpTarget = graph.labelMap.get(((ControlFlowGraph.NodeEndJump) fallthroughTerminator.end).target);
                if (jumpTarget == node) {
                    outStatements.add(new WhileStmt(branch.memoCondition.value, block));
                    isLoop = true;
                } else {
                    throw new RuntimeException("not reached");
                }
            }
            if (!isLoop) {
                outStatements.add(new IfStmt(branch.memoCondition.value, block, null));
            }
            emitted.addAll(fallEmission.toArrayList());
            emitted.add(node);
            outStatements.addAll(targetBlock);
            emitted.addAll(targetEmission.toArrayList());
        } else if (node.end instanceof ControlFlowGraph.NodeEndThrow) {
            emitted.add(node);
            outStatements.add(new ThrowStmt(((ControlFlowGraph.NodeEndThrow) node.end).memoException.value));
        } else if (node.end instanceof ControlFlowGraph.NodeEndMonitorEnter) {
            // TODO:
        } else if (node.end instanceof ControlFlowGraph.NodeEndMonitorExit) {
            // TODO:
        } else if (node.end instanceof ControlFlowGraph.NodeEndReturn) {
            emitted.add(node);
            outStatements.addAll(this.basicBlocks.get(node));
        } else if (node.end instanceof ControlFlowGraph.NodeEndLookupswitch) {
            // TODO
        } else if (node.end instanceof ControlFlowGraph.NodeEndTableswitch) {
            // TODO
        } else if (node.end instanceof ControlFlowGraph.NodeEndCallSub) {
            // TODO
        } else if (node.end instanceof ControlFlowGraph.NodeEndRetSub) {
            // TODO
        } else if (node.end == null) {
            emitted.add(node);
            outStatements.addAll(this.basicBlocks.get(node));
        } else {
            throw new RuntimeException("not reached");
        }
        var emittedList = ImmutableList.from(emitted);
        memoEmission.put(node, emittedList);
        return emittedList;
    }
}
