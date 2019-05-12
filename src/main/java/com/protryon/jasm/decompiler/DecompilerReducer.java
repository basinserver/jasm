package com.protryon.jasm.decompiler;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.protryon.jasm.*;
import com.protryon.jasm.instruction.StackReducer;
import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.Tuple3;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DecompilerReducer extends StackReducer<StackEntry<Expression>> {

    private final Classpath classpath;
    private final Method method;
    private final LocalContext context;
    private final Consumer<Statement> emitter;

    public DecompilerReducer(Classpath classpath, Method method, LocalContext context, Consumer<Statement> emitter) {
        this.classpath = classpath;
        this.method = method;
        this.context = context;
        this.emitter = emitter;
    }

    private static StackEntry<Expression> entry(JType type, Expression expr) {
        return new StackEntry<>(type, expr);
    }

    @Override
    public StackEntry<Expression> reduceAaload(Aaload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        index.type.assertAssignableTo(JType.intT);
        return entry(arrayref.type.elementType(), new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public void reduceAastore(Aastore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType();
        index.type.assertAssignableTo(JType.intT);
        value.type.assertReference();
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceAconst_null(Aconst_null instruction) {
        return entry(JType.nullT, new NullLiteralExpr());
    }

    @Override
    public StackEntry<Expression> reduceAload(Aload instruction) {
        return entry(context.getOrMakeLocal(instruction.index, JType.nullT).type, new NameExpr("v" + instruction.index));
    }

    @Override
    public StackEntry<Expression> reduceAload_0(Aload_0 instruction) {
        if (method.isStatic) {
            return context.getOrMakeLocal(0, JType.nullT).stackify();
        } else {
            return entry(context.getOrMakeLocal(0, JType.instance(method.parent)).type, new ThisExpr());
        }
    }

    @Override
    public StackEntry<Expression> reduceAload_1(Aload_1 instruction) {
        return entry(context.getOrMakeLocal(1, JType.nullT).type, new NameExpr("v1"));
    }

    @Override
    public StackEntry<Expression> reduceAload_2(Aload_2 instruction) {
        return entry(context.getOrMakeLocal(2, JType.nullT).type, new NameExpr("v2"));
    }

    @Override
    public StackEntry<Expression> reduceAload_3(Aload_3 instruction) {
        return entry(context.getOrMakeLocal(3, JType.nullT).type, new NameExpr("v3"));
    }

    @Override
    public StackEntry<Expression> reduceAnewarray(Anewarray instruction, StackEntry<Expression> count) {
        JType type = ((Constant<JType>) instruction.indexbyte).value;
        count.type.assertAssignableTo(JType.intT);
        return entry(JType.array(type), new ArrayCreationExpr(Decompiler.convertType(type), new NodeList<>(new ArrayCreationLevel(count.value)), null));
    }

    @Override
    public void reduceAreturn(Areturn instruction, StackEntry<Expression> objectref) {
        objectref.type.assertReference();
        emitter.accept(new ReturnStmt(objectref.value));
    }

    @Override
    public StackEntry<Expression> reduceArraylength(Arraylength instruction, StackEntry<Expression> arrayref) {
        arrayref.type.elementType();
        return entry(JType.intT, new FieldAccessExpr(arrayref.value, "length"));
    }

    @Override
    public void reduceAstore(Astore instruction, StackEntry<Expression> objectref) {
        context.getOrMakeLocal(instruction.index, objectref.type);
        objectref.type.assertReference();
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v" + instruction.index), objectref.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceAstore_0(Astore_0 instruction, StackEntry<Expression> objectref) {
        context.getOrMakeLocal(0, objectref.type);
        objectref.type.assertReference();
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v0"), objectref.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceAstore_1(Astore_1 instruction, StackEntry<Expression> objectref) {
        context.getOrMakeLocal(1, objectref.type);
        objectref.type.assertReference();
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v1"), objectref.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceAstore_2(Astore_2 instruction, StackEntry<Expression> objectref) {
        context.getOrMakeLocal(2, objectref.type);
        objectref.type.assertReference();
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v2"), objectref.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceAstore_3(Astore_3 instruction, StackEntry<Expression> objectref) {
        context.getOrMakeLocal(3, objectref.type);
        objectref.type.assertReference();
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v3"), objectref.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceAthrow(Athrow instruction, StackEntry<Expression> objectref) {
        objectref.type.assertReference();
        emitter.accept(new ThrowStmt(objectref.value));
        return entry(objectref.type, new NameExpr("t" + method.tempVariableCounter++));
    }

    @Override
    public StackEntry<Expression> reduceBaload(Baload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        index.type.assertAssignableTo(JType.intT);
        return entry(arrayref.type.elementType(), new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public void reduceBastore(Bastore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType();
        index.type.assertAssignableTo(JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceBipush(Bipush instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(instruction.byte_));
    }

    @Override
    public StackEntry<Expression> reduceCaload(Caload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        arrayref.type.elementType().assertAssignableTo(JType.charT);
        index.type.assertAssignableTo(JType.intT);
        return entry(JType.charT, new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public void reduceCastore(Castore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType().assertAssignableTo(JType.charT);
        index.type.assertAssignableTo(JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceCheckcast(Checkcast instruction, StackEntry<Expression> objectref) {
        objectref.type.assertReference();
        JType type = ((Constant<JType>) instruction.indexbyte).value;
        return entry(type, new CastExpr(Decompiler.convertType(type), objectref.value));
    }

    @Override
    public StackEntry<Expression> reduceD2f(D2f instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.doubleT);
        return entry(JType.floatT, new CastExpr(PrimitiveType.floatType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceD2i(D2i instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.doubleT);
        return entry(JType.floatT, new CastExpr(PrimitiveType.intType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceD2l(D2l instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.doubleT);
        return entry(JType.floatT, new CastExpr(PrimitiveType.longType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceDadd(Dadd instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.doubleT);
        value2.type.assertAssignableTo(JType.doubleT);
        return entry(JType.doubleT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.PLUS));
    }

    @Override
    public StackEntry<Expression> reduceDaload(Daload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        arrayref.type.elementType().assertAssignableTo(JType.doubleT);
        index.type.assertAssignableTo(JType.intT);
        return entry(JType.doubleT, new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public void reduceDastore(Dastore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType().assertAssignableTo(JType.doubleT);
        index.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceDcmpg(Dcmpg instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        return entry(JType.intT, new MethodCallExpr(new NameExpr("Double"), "compareTo", new NodeList<>(value1.value, value2.value)));
    }

    @Override
    public StackEntry<Expression> reduceDcmpl(Dcmpl instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        // NaN is negative infinity not positive infinity
        return entry(JType.intT, new UnaryExpr(new MethodCallExpr(new NameExpr("Double"), "compareTo", new NodeList<>(value2.value, value1.value)), UnaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceDconst_0(Dconst_0 instruction) {
        return entry(JType.doubleT, new DoubleLiteralExpr(0D));
    }

    @Override
    public StackEntry<Expression> reduceDconst_1(Dconst_1 instruction) {
        return entry(JType.doubleT, new DoubleLiteralExpr(1D));
    }

    @Override
    public StackEntry<Expression> reduceDdiv(Ddiv instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        return entry(JType.doubleT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE));
    }

    @Override
    public StackEntry<Expression> reduceDload(Dload instruction) {
        context.getOrMakeLocal(instruction.index, JType.doubleT);
        return entry(JType.doubleT, new NameExpr("v" + instruction.index));
    }

    @Override
    public StackEntry<Expression> reduceDload_0(Dload_0 instruction) {
        context.getOrMakeLocal(0, JType.doubleT);
        return entry(JType.doubleT, new NameExpr("v0"));
    }

    @Override
    public StackEntry<Expression> reduceDload_1(Dload_1 instruction) {
        context.getOrMakeLocal(1, JType.doubleT);
        return entry(JType.doubleT, new NameExpr("v1"));
    }

    @Override
    public StackEntry<Expression> reduceDload_2(Dload_2 instruction) {
        context.getOrMakeLocal(2, JType.doubleT);
        return entry(JType.doubleT, new NameExpr("v2"));
    }

    @Override
    public StackEntry<Expression> reduceDload_3(Dload_3 instruction) {
        context.getOrMakeLocal(3, JType.doubleT);
        return entry(JType.doubleT, new NameExpr("v3"));
    }

    @Override
    public StackEntry<Expression> reduceDmul(Dmul instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        return entry(JType.doubleT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY));
    }

    @Override
    public StackEntry<Expression> reduceDneg(Dneg instruction, StackEntry<Expression> value) {
        return entry(JType.doubleT, new UnaryExpr(value.value, UnaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceDrem(Drem instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        return entry(JType.doubleT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER));
    }

    @Override
    public void reduceDreturn(Dreturn instruction, StackEntry<Expression> value) {
        emitter.accept(new ReturnStmt(value.value));
    }

    @Override
    public void reduceDstore(Dstore instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(instruction.index, JType.doubleT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceDstore_0(Dstore_0 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(0, JType.doubleT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceDstore_1(Dstore_1 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(1, JType.doubleT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceDstore_2(Dstore_2 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(2, JType.doubleT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceDstore_3(Dstore_3 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(3, JType.doubleT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceDsub(Dsub instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.doubleT);
        value2.type.assertAssignableTo(JType.doubleT);
        return entry(JType.doubleT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS));
    }

    @Override
    public Pair<StackEntry<Expression>, StackEntry<Expression>> reduceDup(Dup instruction, StackEntry<Expression> value) {
        return Pair.of(value, value);
    }

    @Override
    public Tuple3<StackEntry<Expression>, StackEntry<Expression>, StackEntry<Expression>> reduceDup_x1(Dup_x1 instruction, StackEntry<Expression> value2, StackEntry<Expression> value1) {
        return Tuple3.of(value1, value2, value1);
    }

    @Override
    public ImmutableList<StackEntry<Expression>> reduceDup_x2(Dup_x2 instruction, StackEntry<Expression> value1, StackEntry<Expression> value2, Maybe<StackEntry<Expression>> value3) {
        value1.type.assertComputationType(1);
        if (value2.type.computationType == 1) {
            return ImmutableList.of(value1, value3.fromJust(), value2, value1);
        } else {
            return ImmutableList.of(value1, value2, value1);
        }
    }

    @Override
    public ImmutableList<StackEntry<Expression>> reduceDup2(Dup2 instruction, StackEntry<Expression> value1, Maybe<StackEntry<Expression>> value2) {
        if (value1.type.computationType == 1) {
            return ImmutableList.of(value1, value2.fromJust(), value1, value2.fromJust());
        } else {
            return ImmutableList.of(value1, value1);
        }
    }

    @Override
    public ImmutableList<StackEntry<Expression>> reduceDup2_x1(Dup2_x1 instruction, StackEntry<Expression> value1, StackEntry<Expression> value2, Maybe<StackEntry<Expression>> value3) {
        throw new UnsupportedOperationException("Dup2_x1");
    }

    @Override
    public ImmutableList<StackEntry<Expression>> reduceDup2_x2(Dup2_x2 instruction, StackEntry<Expression> value1, StackEntry<Expression> value2, Maybe<StackEntry<Expression>> value3, Maybe<StackEntry<Expression>> value4) {
        throw new UnsupportedOperationException("Dup2_x2");
    }

    @Override
    public StackEntry<Expression> reduceF2d(F2d instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.floatT);
        return entry(JType.doubleT, new CastExpr(PrimitiveType.doubleType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceF2i(F2i instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.floatT);
        return entry(JType.intT, new CastExpr(PrimitiveType.intType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceF2l(F2l instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.floatT);
        return entry(JType.longT, new CastExpr(PrimitiveType.longType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceFadd(Fadd instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.floatT);
        value2.type.assertAssignableTo(JType.floatT);
        return entry(JType.floatT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.PLUS));
    }

    @Override
    public StackEntry<Expression> reduceFaload(Faload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        arrayref.type.elementType().assertAssignableTo(JType.floatT);
        index.type.assertAssignableTo(JType.intT);
        return entry(JType.floatT, new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public void reduceFastore(Fastore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType().assertAssignableTo(JType.floatT);
        index.type.assertAssignableTo(JType.intT);
        value.type.assertAssignableTo(JType.floatT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceFcmpg(Fcmpg instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.floatT);
        value2.type.assertAssignableTo(JType.floatT);
        return entry(JType.intT, new MethodCallExpr(new NameExpr("Float"), "compareTo", new NodeList<>(value1.value, value2.value)));
    }

    @Override
    public StackEntry<Expression> reduceFcmpl(Fcmpl instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        // NaN is negative infinity not positive infinity
        value2.type.assertAssignableTo(JType.floatT);
        value1.type.assertAssignableTo(JType.floatT);
        return entry(JType.intT, new UnaryExpr(new MethodCallExpr(new NameExpr("Float"), "compareTo", new NodeList<>(value2.value, value1.value)), UnaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceFconst_0(Fconst_0 instruction) {
        return entry(JType.floatT, new DoubleLiteralExpr(0F)); // TODO: we should somehow make this codegen as 0F not 0D
    }

    @Override
    public StackEntry<Expression> reduceFconst_1(Fconst_1 instruction) {
        return entry(JType.floatT, new DoubleLiteralExpr(1F));
    }

    @Override
    public StackEntry<Expression> reduceFconst_2(Fconst_2 instruction) {
        return entry(JType.floatT, new DoubleLiteralExpr(2F));
    }

    @Override
    public StackEntry<Expression> reduceFdiv(Fdiv instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.floatT);
        value2.type.assertAssignableTo(JType.floatT);
        return entry(JType.floatT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE));
    }

    @Override
    public StackEntry<Expression> reduceFload(Fload instruction) {
        context.getOrMakeLocal(instruction.index, JType.floatT);
        return entry(JType.floatT, new NameExpr("v" + instruction.index));
    }

    @Override
    public StackEntry<Expression> reduceFload_0(Fload_0 instruction) {
        context.getOrMakeLocal(0, JType.floatT);
        return entry(JType.floatT, new NameExpr("v0"));
    }

    @Override
    public StackEntry<Expression> reduceFload_1(Fload_1 instruction) {
        context.getOrMakeLocal(1, JType.floatT);
        return entry(JType.floatT, new NameExpr("v1"));
    }

    @Override
    public StackEntry<Expression> reduceFload_2(Fload_2 instruction) {
        context.getOrMakeLocal(2, JType.floatT);
        return entry(JType.floatT, new NameExpr("v2"));
    }

    @Override
    public StackEntry<Expression> reduceFload_3(Fload_3 instruction) {
        context.getOrMakeLocal(3, JType.floatT);
        return entry(JType.floatT, new NameExpr("v3"));
    }

    @Override
    public StackEntry<Expression> reduceFmul(Fmul instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.floatT);
        value2.type.assertAssignableTo(JType.floatT);
        return entry(JType.floatT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY));
    }

    @Override
    public StackEntry<Expression> reduceFneg(Fneg instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.floatT);
        return entry(JType.floatT, new UnaryExpr(value.value, UnaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceFrem(Frem instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.floatT);
        value2.type.assertAssignableTo(JType.floatT);
        return entry(JType.floatT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER));
    }

    @Override
    public void reduceFreturn(Freturn instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.floatT);
        emitter.accept(new ReturnStmt(value.value));
    }

    @Override
    public void reduceFstore(Fstore instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(instruction.index, JType.floatT);
        value.type.assertAssignableTo(JType.floatT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceFstore_0(Fstore_0 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(0, JType.floatT);
        value.type.assertAssignableTo(JType.floatT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceFstore_1(Fstore_1 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(1, JType.floatT);
        value.type.assertAssignableTo(JType.floatT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceFstore_2(Fstore_2 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(2, JType.floatT);
        value.type.assertAssignableTo(JType.floatT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceFstore_3(Fstore_3 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(3, JType.floatT);
        value.type.assertAssignableTo(JType.floatT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceFsub(Fsub instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.floatT);
        value2.type.assertAssignableTo(JType.floatT);
        return entry(JType.floatT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceGetfield(Getfield instruction, StackEntry<Expression> objectref) {
        Field field = ((Constant<Field>) instruction.indexbyte).value;
        return entry(field.type, new FieldAccessExpr(objectref.value, field.name));
    }

    @Override
    public StackEntry<Expression> reduceGetstatic(Getstatic instruction) {
        Field field = ((Constant<Field>) instruction.indexbyte).value;
        return entry(field.type, new FieldAccessExpr(new TypeExpr(new ClassOrInterfaceType(field.parent.name)), field.name));
    }

    @Override
    public void reduceGoto(Goto instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceGoto_w(Goto_w instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public StackEntry<Expression> reduceI2b(I2b instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        return entry(JType.byteT, new CastExpr(PrimitiveType.byteType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceI2c(I2c instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        return entry(JType.charT, new CastExpr(PrimitiveType.charType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceI2d(I2d instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        return entry(JType.doubleT, new CastExpr(PrimitiveType.doubleType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceI2f(I2f instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        return entry(JType.floatT, new CastExpr(PrimitiveType.floatType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceI2l(I2l instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        return entry(JType.longT, new CastExpr(PrimitiveType.longType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceI2s(I2s instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        return entry(JType.shortT, new CastExpr(PrimitiveType.shortType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceIadd(Iadd instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceIaload(Iaload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        arrayref.type.elementType().assertAssignableTo(JType.intT);
        index.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public StackEntry<Expression> reduceIand(Iand instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_AND));
    }

    @Override
    public void reduceIastore(Iastore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType().assertAssignableTo(JType.intT);
        index.type.assertAssignableTo(JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceIconst_m1(Iconst_m1 instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(-1));
    }

    @Override
    public StackEntry<Expression> reduceIconst_0(Iconst_0 instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(0));
    }

    @Override
    public StackEntry<Expression> reduceIconst_1(Iconst_1 instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(1));
    }

    @Override
    public StackEntry<Expression> reduceIconst_2(Iconst_2 instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(2));
    }

    @Override
    public StackEntry<Expression> reduceIconst_3(Iconst_3 instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(3));
    }

    @Override
    public StackEntry<Expression> reduceIconst_4(Iconst_4 instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(4));
    }

    @Override
    public StackEntry<Expression> reduceIconst_5(Iconst_5 instruction) {
        return entry(JType.intT, new IntegerLiteralExpr(5));
    }

    @Override
    public StackEntry<Expression> reduceIdiv(Idiv instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE));
    }

    @Override
    public void reduceIf_acmpeq(If_acmpeq instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIf_acmpne(If_acmpne instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIf_icmpeq(If_icmpeq instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIf_icmpne(If_icmpne instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIf_icmplt(If_icmplt instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIf_icmpge(If_icmpge instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIf_icmpgt(If_icmpgt instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIf_icmple(If_icmple instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIfeq(Ifeq instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIfne(Ifne instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIflt(Iflt instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIfge(Ifge instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIfgt(Ifgt instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIfle(Ifle instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIfnonnull(Ifnonnull instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIfnull(Ifnull instruction, StackEntry<Expression> value) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceIinc(Iinc instruction) {
        // TODO: this is unsafe, must be inlined probably
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v" + instruction.index), new IntegerLiteralExpr(instruction.const_), AssignExpr.Operator.PLUS)));
    }

    @Override
    public StackEntry<Expression> reduceIload(Iload instruction) {
        context.getOrMakeLocal(instruction.index, JType.intT);
        return entry(JType.intT, new NameExpr("v" + instruction.index));
    }

    @Override
    public StackEntry<Expression> reduceIload_0(Iload_0 instruction) {
        context.getOrMakeLocal(0, JType.intT);
        return entry(JType.intT, new NameExpr("v0"));
    }

    @Override
    public StackEntry<Expression> reduceIload_1(Iload_1 instruction) {
        context.getOrMakeLocal(1, JType.intT);
        return entry(JType.intT, new NameExpr("v1"));
    }

    @Override
    public StackEntry<Expression> reduceIload_2(Iload_2 instruction) {
        context.getOrMakeLocal(2, JType.intT);
        return entry(JType.intT, new NameExpr("v2"));
    }

    @Override
    public StackEntry<Expression> reduceIload_3(Iload_3 instruction) {
        context.getOrMakeLocal(3, JType.intT);
        return entry(JType.intT, new NameExpr("v3"));
    }

    @Override
    public StackEntry<Expression> reduceImul(Imul instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY));
    }

    @Override
    public StackEntry<Expression> reduceIneg(Ineg instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new UnaryExpr(value.value, UnaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceInstanceof(Instanceof instruction, StackEntry<Expression> objectref) {
        return entry(JType.booleanT, new InstanceOfExpr(objectref.value, (ReferenceType) Decompiler.convertType(((Constant<JType>) instruction.indexbyte).value)));
    }

    @Override
    public StackEntry<Expression> reduceIor(Ior instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_OR));
    }

    @Override
    public StackEntry<Expression> reduceIrem(Irem instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER));
    }

    @Override
    public void reduceIreturn(Ireturn instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ReturnStmt(value.value));
    }

    @Override
    public StackEntry<Expression> reduceIshl(Ishl instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LEFT_SHIFT));
    }

    @Override
    public StackEntry<Expression> reduceIshr(Ishr instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.SIGNED_RIGHT_SHIFT));
    }

    @Override
    public void reduceIstore(Istore instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(instruction.index, JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceIstore_0(Istore_0 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(0, JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceIstore_1(Istore_1 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(1, JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceIstore_2(Istore_2 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(2, JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceIstore_3(Istore_3 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(3, JType.intT);
        value.type.assertAssignableTo(JType.intT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceIsub(Isub instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceIushr(Iushr instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT));
    }

    @Override
    public StackEntry<Expression> reduceIxor(Ixor instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.intT);
        value2.type.assertAssignableTo(JType.intT);
        return entry(JType.intT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.XOR));
    }

    @Override
    public StackEntry<Expression> reduceJsr(Jsr instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public StackEntry<Expression> reduceJsr_w(Jsr_w instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public StackEntry<Expression> reduceL2d(L2d instruction, StackEntry<Expression> value) {
        return value;
    }

    @Override
    public StackEntry<Expression> reduceL2f(L2f instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.longT);
        return entry(JType.floatT, new CastExpr(PrimitiveType.floatType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceL2i(L2i instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.longT);
        return entry(JType.intT, new CastExpr(PrimitiveType.intType(), value.value));
    }

    @Override
    public StackEntry<Expression> reduceLadd(Ladd instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.PLUS));
    }

    @Override
    public StackEntry<Expression> reduceLaload(Laload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        arrayref.type.elementType().assertAssignableTo(JType.longT);
        index.type.assertAssignableTo(JType.intT);
        return entry(JType.longT, new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public StackEntry<Expression> reduceLand(Land instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_AND));
    }

    @Override
    public void reduceLastore(Lastore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType().assertAssignableTo(JType.longT);
        index.type.assertAssignableTo(JType.intT);
        value.type.assertAssignableTo(JType.longT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceLcmp(Lcmp instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.intT, new MethodCallExpr(new NameExpr("Long"), "compareTo", new NodeList<>(value1.value, value2.value)));
    }

    @Override
    public StackEntry<Expression> reduceLconst_0(Lconst_0 instruction) {
        return entry(JType.longT, new LongLiteralExpr(0L));
    }

    @Override
    public StackEntry<Expression> reduceLconst_1(Lconst_1 instruction) {
        return entry(JType.longT, new LongLiteralExpr(-1L));
    }

    private StackEntry<Expression> loadConstant(Constant constant) {
        if (constant.value instanceof Byte) {
            return entry(JType.byteT, new IntegerLiteralExpr((Byte) constant.value));
        } else if (constant.value instanceof Short) {
            return entry(JType.shortT, new IntegerLiteralExpr((Short) constant.value));
        } else if (constant.value instanceof Integer) {
            return entry(JType.intT, new IntegerLiteralExpr((Integer) constant.value));
        } else if (constant.value instanceof Long) {
            return entry(JType.longT, new LongLiteralExpr((Long) constant.value));
        } else if (constant.value instanceof Float) {
            return entry(JType.floatT, new DoubleLiteralExpr((Float) constant.value));
        } else if (constant.value instanceof Double) {
            return entry(JType.doubleT, new DoubleLiteralExpr((Double) constant.value));
        } else if (constant.value instanceof String) {
            return entry(JType.instance(classpath.loadKlass("java/lang/String")), new StringLiteralExpr((String) constant.value));
        } else if (constant.value instanceof JType) {
            return entry(JType.instance(classpath.loadKlass("java/lang/Class")), new FieldAccessExpr(new TypeExpr(Decompiler.convertType((JType) constant.value)), "class"));
        } else {
            throw new UnsupportedOperationException("Unexpected literal type loaded: " + constant.value.getClass().getSimpleName());
        }
    }

    @Override
    public StackEntry<Expression> reduceLdc(Ldc instruction) {
        return loadConstant(instruction.index);
    }

    @Override
    public StackEntry<Expression> reduceLdc_w(Ldc_w instruction) {
        return loadConstant(instruction.indexbyte);
    }

    @Override
    public StackEntry<Expression> reduceLdc2_w(Ldc2_w instruction) {
        return loadConstant(instruction.indexbyte);
    }

    @Override
    public StackEntry<Expression> reduceLdiv(Ldiv instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE));
    }

    @Override
    public StackEntry<Expression> reduceLload(Lload instruction) {
        context.getOrMakeLocal(instruction.index, JType.longT);
        return entry(JType.longT, new NameExpr("v" + instruction.index));
    }

    @Override
    public StackEntry<Expression> reduceLload_0(Lload_0 instruction) {
        context.getOrMakeLocal(0, JType.longT);
        return entry(JType.longT, new NameExpr("v0"));
    }

    @Override
    public StackEntry<Expression> reduceLload_1(Lload_1 instruction) {
        context.getOrMakeLocal(1, JType.longT);
        return entry(JType.longT, new NameExpr("v1"));
    }

    @Override
    public StackEntry<Expression> reduceLload_2(Lload_2 instruction) {
        context.getOrMakeLocal(2, JType.longT);
        return entry(JType.longT, new NameExpr("v2"));
    }

    @Override
    public StackEntry<Expression> reduceLload_3(Lload_3 instruction) {
        context.getOrMakeLocal(3, JType.longT);
        return entry(JType.longT, new NameExpr("v3"));
    }

    @Override
    public StackEntry<Expression> reduceLmul(Lmul instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY));
    }

    @Override
    public StackEntry<Expression> reduceLneg(Lneg instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new UnaryExpr(value.value, UnaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceLor(Lor instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_OR));
    }

    @Override
    public StackEntry<Expression> reduceLrem(Lrem instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER));
    }

    @Override
    public void reduceLreturn(Lreturn instruction, StackEntry<Expression> value) {
        value.type.assertAssignableTo(JType.longT);
        emitter.accept(new ReturnStmt(value.value));
    }

    @Override
    public StackEntry<Expression> reduceLshl(Lshl instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LEFT_SHIFT));
    }

    @Override
    public StackEntry<Expression> reduceLshr(Lshr instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.SIGNED_RIGHT_SHIFT));
    }

    @Override
    public void reduceLstore(Lstore instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(instruction.index, JType.longT);
        value.type.assertAssignableTo(JType.longT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceLstore_0(Lstore_0 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(0, JType.longT);
        value.type.assertAssignableTo(JType.longT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceLstore_1(Lstore_1 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(1, JType.longT);
        value.type.assertAssignableTo(JType.longT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceLstore_2(Lstore_2 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(2, JType.longT);
        value.type.assertAssignableTo(JType.longT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceLstore_3(Lstore_3 instruction, StackEntry<Expression> value) {
        context.getOrMakeLocal(03, JType.longT);
        value.type.assertAssignableTo(JType.longT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceLsub(Lsub instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS));
    }

    @Override
    public StackEntry<Expression> reduceLushr(Lushr instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT));
    }

    @Override
    public StackEntry<Expression> reduceLxor(Lxor instruction, StackEntry<Expression> value1, StackEntry<Expression> value2) {
        value1.type.assertAssignableTo(JType.longT);
        value2.type.assertAssignableTo(JType.longT);
        return entry(JType.longT, new BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.XOR));
    }

    @Override
    public void reduceMonitorenter(Monitorenter instruction, StackEntry<Expression> objectref) {
        throw new RuntimeException("should only be run in basic blocks, monitorenter variant");
    }

    @Override
    public void reduceMonitorexit(Monitorexit instruction, StackEntry<Expression> objectref) {
        throw new RuntimeException("should only be run in basic blocks, monitorexit variant");
    }

    @Override
    public StackEntry<Expression> reduceMultianewarray(Multianewarray instruction, List<StackEntry<Expression>> dimensions) {
        JType type = ((Constant<JType>) instruction.indexbyte).value;
        List<ArrayCreationLevel> levels = dimensions.stream().map(expr -> {
            expr.type.assertAssignableTo(JType.intT);
            return new ArrayCreationLevel(expr.value);
        }).collect(Collectors.toList());
        return entry(JType.array(type), new ArrayCreationExpr(Decompiler.convertType(type), new NodeList<>(levels), null));
    }

    @Override
    public StackEntry<Expression> reduceNew(New instruction) {
        JType type = ((Constant<JType>) instruction.indexbyte).value;
        return entry(type, new ObjectCreationExpr(null, (ClassOrInterfaceType) Decompiler.convertType(type), new NodeList<>()));
    }

    @Override
    public StackEntry<Expression> reduceNewarray(Newarray instruction, StackEntry<Expression> count) {
        count.type.assertAssignableTo(JType.intT);
        JType arrayType = JType.array(instruction.atype.type);
        return entry(arrayType, new ArrayCreationExpr(Decompiler.convertType(arrayType), new NodeList<>(new ArrayCreationLevel(count.value)), null));
    }

    @Override
    public void reduceNop(Nop instruction) {
        // nop
    }

    @Override
    public void reducePop(Pop instruction, StackEntry<Expression> value) {
        emitter.accept(new ExpressionStmt(value.value));
    }

    @Override
    public void reducePop2(Pop2 instruction, StackEntry<Expression> value1, Maybe<StackEntry<Expression>> value2) {
        emitter.accept(new ExpressionStmt(value1.value));
        if (value2.isJust()) {
            emitter.accept(new ExpressionStmt(value2.fromJust().value));
        }
    }

    @Override
    public void reducePutfield(Putfield instruction, StackEntry<Expression> objectref, StackEntry<Expression> value) {
        Field field = ((Constant<Field>) instruction.indexbyte).value;
        objectref.type.assertReference();
        value.type.assertAssignableTo(field.type);
        emitter.accept(new ExpressionStmt(new AssignExpr(new FieldAccessExpr(objectref.value, field.name), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reducePutstatic(Putstatic instruction, StackEntry<Expression> value) {
        Field field = ((Constant<Field>) instruction.indexbyte).value;
        value.type.assertAssignableTo(field.type);
        emitter.accept(new ExpressionStmt(new AssignExpr(new FieldAccessExpr(new TypeExpr(Decompiler.convertType(JType.instance(field.parent))), field.name), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public void reduceRet(Ret instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceReturn(Return instruction) {
        emitter.accept(new ReturnStmt());
    }

    @Override
    public StackEntry<Expression> reduceSaload(Saload instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index) {
        arrayref.type.elementType().assertAssignableTo(JType.shortT);
        index.type.assertAssignableTo(JType.intT);
        return entry(JType.shortT, new ArrayAccessExpr(arrayref.value, index.value));
    }

    @Override
    public void reduceSastore(Sastore instruction, StackEntry<Expression> arrayref, StackEntry<Expression> index, StackEntry<Expression> value) {
        arrayref.type.elementType().assertAssignableTo(JType.shortT);
        index.type.assertAssignableTo(JType.intT);
        value.type.assertAssignableTo(JType.shortT);
        emitter.accept(new ExpressionStmt(new AssignExpr(new ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)));
    }

    @Override
    public StackEntry<Expression> reduceSipush(Sipush instruction) {
        return entry(JType.shortT, new IntegerLiteralExpr(instruction.byte_));
    }

    @Override
    public Pair<StackEntry<Expression>, StackEntry<Expression>> reduceSwap(Swap instruction, StackEntry<Expression> value2, StackEntry<Expression> value1) {
        //TODO: execution order is messed up here no doubt
        return Pair.of(value1, value2);
    }

    private Maybe<StackEntry<Expression>> instanceInvokation(StackEntry<Expression> objectref, List<StackEntry<Expression>> arguments, Method method) {
        MethodCallExpr expr = new MethodCallExpr(objectref.value, method.name, new NodeList<>(arguments.stream().map(entry -> entry.value).collect(Collectors.toList())));
        if (method.descriptor.returnType == JType.voidT) {
            emitter.accept(new ExpressionStmt(expr));
            return Maybe.empty();
        } else {
            return Maybe.of(entry(method.descriptor.returnType, expr));
        }
    }

    @Override
    public Maybe<StackEntry<Expression>> reduceInvokeinterface(Invokeinterface instruction, StackEntry<Expression> objectref, List<StackEntry<Expression>> arguments) {
        return instanceInvokation(objectref, arguments, ((Constant<Method>) instruction.indexbyte).value);
    }

    @Override
    public Maybe<StackEntry<Expression>> reduceInvokestatic(Invokestatic instruction, List<StackEntry<Expression>> arguments) {
        Method method = ((Constant<Method>) instruction.indexbyte).value;
        MethodCallExpr expr = new MethodCallExpr(new TypeExpr(Decompiler.convertType(JType.instance(method.parent))), method.name, new NodeList<>(arguments.stream().map(entry -> entry.value).collect(Collectors.toList())));
        if (method.descriptor.returnType == JType.voidT) {
            emitter.accept(new ExpressionStmt(expr));
            return Maybe.empty();
        } else {
            return Maybe.of(entry(method.descriptor.returnType, expr));
        }
    }

    @Override
    public Maybe<StackEntry<Expression>> reduceInvokevirtual(Invokevirtual instruction, StackEntry<Expression> objectref, List<StackEntry<Expression>> arguments) {
        return instanceInvokation(objectref, arguments, ((Constant<Method>) instruction.indexbyte).value);
    }

    @Override
    public Maybe<StackEntry<Expression>> reduceInvokedynamic(Invokedynamic instruction, List<StackEntry<Expression>> arguments) {
        Dynamic dynamic = ((Constant<Dynamic>) instruction.indexbyte).value;
        MethodDescriptor descriptor = dynamic.nameAndType.type.right().fromJust();
        //     public MethodReferenceExpr(Expression scope, NodeList<Type> typeArguments, String identifier) {
        //        this((TokenRange)null, scope, typeArguments, identifier);
        //
        MethodHandle subHandle = ((Constant<MethodHandle>) dynamic.bootstrapMethod.arguments[1]).value;
        MethodHandle.MethodHandleType type = subHandle.refType;
        Expression scope;
        String handleName;
        if (type == MethodHandle.MethodHandleType.REF_getField ||
            type == MethodHandle.MethodHandleType.REF_putField) {
            //TODO: this logic is a hack
            scope = arguments.size() == 0 ?
                new TypeExpr(Decompiler.convertType(JType.instance(((Constant<Field>) subHandle.ref).value.parent))) :
                arguments.get(0).value;
            handleName = ((Constant<Field>) subHandle.ref).value.name;
        } else if (type == MethodHandle.MethodHandleType.REF_invokeVirtual ||
            type == MethodHandle.MethodHandleType.REF_invokeSpecial ||
            type == MethodHandle.MethodHandleType.REF_newInvokeSpecial ||
            type == MethodHandle.MethodHandleType.REF_invokeInterface) {
            //TODO: this logic is a hack
            scope = arguments.size() == 0 ?
                new TypeExpr(Decompiler.convertType(JType.instance(((Constant<Method>) subHandle.ref).value.parent))) :
                arguments.get(0).value;
            handleName = ((Constant<Method>) subHandle.ref).value.name;
        } else if (type == MethodHandle.MethodHandleType.REF_getStatic ||
            type == MethodHandle.MethodHandleType.REF_putStatic) {
            scope = new TypeExpr(Decompiler.convertType(JType.instance(((Constant<Field>) subHandle.ref).value.parent)));
            handleName = ((Constant<Field>) subHandle.ref).value.name;
        } else if (type == MethodHandle.MethodHandleType.REF_invokeStatic) {
            scope = new TypeExpr(Decompiler.convertType(JType.instance(((Constant<Method>) subHandle.ref).value.parent)));
            handleName = ((Constant<Method>) subHandle.ref).value.name;
        } else {
            throw new RuntimeException("Unexpected method handle type: " + type.toString());
        }
        MethodReferenceExpr expr = new MethodReferenceExpr(scope, new NodeList<>(), handleName);
        return Maybe.of(entry(descriptor.returnType, expr));
    }

    @Override
    public Maybe<StackEntry<Expression>> reduceInvokespecial(Invokespecial instruction, StackEntry<Expression> objectref, List<StackEntry<Expression>> arguments) {
        Method method = ((Constant<Method>) instruction.indexbyte).value;
        if (objectref.value instanceof ObjectCreationExpr) {
            if (method.descriptor.returnType != JType.voidT) {
                throw new RuntimeException("unexpected return type from invokespecial: " + method.descriptor.toString());
            }
            ((ObjectCreationExpr) objectref.value).setArguments(new NodeList<>(arguments.stream().map(x -> x.value).collect(Collectors.toList())));
            return Maybe.empty();
        } else {
            return instanceInvokation(objectref, arguments, method);
        }
    }

    @Override
    public void reduceLookupswitch(Lookupswitch instruction, StackEntry<Expression> index) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceTableswitch(Tableswitch instruction, StackEntry<Expression> index) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceEnterTry(EnterTry instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceExitTry(ExitTry instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }

    @Override
    public void reduceLabel(Label instruction) {
        throw new RuntimeException("should only run on basic blocks");
    }
}
