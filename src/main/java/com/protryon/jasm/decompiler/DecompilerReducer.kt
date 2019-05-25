package com.protryon.jasm.decompiler

import com.github.javaparser.ast.ArrayCreationLevel
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.type.*
import com.protryon.jasm.*
import com.protryon.jasm.instruction.StackReducer
import com.protryon.jasm.instruction.instructions.*
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.data.ImmutableList
import java.util.stream.Collector
import java.util.stream.Collectors

class DecompilerReducer(private val classpath: Classpath, private val method: Method, private val context: LocalContext, private val emitter: (Statement)->Unit) : StackReducer<StackEntry<Expression>>() {

    private fun entry(type: JType, expr: Expression): StackEntry<Expression> {
        return StackEntry(type, expr)
    }

    override fun reduceAaload(instruction: Aaload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        index.type.assertAssignableTo(JType.intT)
        return entry(arrayref.type.elementType(), ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceAastore(instruction: Aastore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType()
        index.type.assertAssignableTo(JType.intT)
        value.type.assertReference()
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceAconst_null(instruction: Aconst_null): StackEntry<Expression> {
        return entry(JType.nullT, NullLiteralExpr())
    }

    override fun reduceAload(instruction: Aload): StackEntry<Expression> {
        return entry(context.getOrMakeLocal(instruction.index!!, JType.nullT).type!!, NameExpr("v" + instruction.index))
    }

    override fun reduceAload_0(instruction: Aload_0): StackEntry<Expression> {
        return if (method.isStatic) {
            context.getOrMakeLocal(0, JType.nullT).stackify()
        } else {
            entry(context.getOrMakeLocal(0, JType.instance(method.parent)).type!!, ThisExpr())
        }
    }

    override fun reduceAload_1(instruction: Aload_1): StackEntry<Expression> {
        return entry(context.getOrMakeLocal(1, JType.nullT).type!!, NameExpr("v1"))
    }

    override fun reduceAload_2(instruction: Aload_2): StackEntry<Expression> {
        return entry(context.getOrMakeLocal(2, JType.nullT).type!!, NameExpr("v2"))
    }

    override fun reduceAload_3(instruction: Aload_3): StackEntry<Expression> {
        return entry(context.getOrMakeLocal(3, JType.nullT).type!!, NameExpr("v3"))
    }

    override fun reduceAnewarray(instruction: Anewarray, count: StackEntry<Expression>): StackEntry<Expression> {
        val type = (instruction.indexbyte as Constant<JType>).value
        count.type.assertAssignableTo(JType.intT)
        return entry(JType.array(type), ArrayCreationExpr(Decompiler.convertType(type), NodeList(ArrayCreationLevel(count.value)), null))
    }

    override fun reduceAreturn(instruction: Areturn, objectref: StackEntry<Expression>) {
        objectref.type.assertReference()
        emitter.invoke(ReturnStmt(objectref.value))
    }

    override fun reduceArraylength(instruction: Arraylength, arrayref: StackEntry<Expression>): StackEntry<Expression> {
        arrayref.type.elementType()
        return entry(JType.intT, FieldAccessExpr(arrayref.value, "length"))
    }

    override fun reduceAstore(instruction: Astore, objectref: StackEntry<Expression>) {
        context.getOrMakeLocal(instruction.index!!, objectref.type)
        objectref.type.assertReference()
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v" + instruction.index), objectref.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceAstore_0(instruction: Astore_0, objectref: StackEntry<Expression>) {
        context.getOrMakeLocal(0, objectref.type)
        objectref.type.assertReference()
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v0"), objectref.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceAstore_1(instruction: Astore_1, objectref: StackEntry<Expression>) {
        context.getOrMakeLocal(1, objectref.type)
        objectref.type.assertReference()
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v1"), objectref.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceAstore_2(instruction: Astore_2, objectref: StackEntry<Expression>) {
        context.getOrMakeLocal(2, objectref.type)
        objectref.type.assertReference()
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v2"), objectref.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceAstore_3(instruction: Astore_3, objectref: StackEntry<Expression>) {
        context.getOrMakeLocal(3, objectref.type)
        objectref.type.assertReference()
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v3"), objectref.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceAthrow(instruction: Athrow, objectref: StackEntry<Expression>): StackEntry<Expression> {
        objectref.type.assertReference()
        emitter.invoke(ThrowStmt(objectref.value))
        return entry(objectref.type, NameExpr("t" + method.tempVariableCounter++))
    }

    override fun reduceBaload(instruction: Baload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        index.type.assertAssignableTo(JType.intT)
        return entry(arrayref.type.elementType(), ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceBastore(instruction: Bastore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType()
        index.type.assertAssignableTo(JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceBipush(instruction: Bipush): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(instruction.byte_!!))
    }

    override fun reduceCaload(instruction: Caload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        arrayref.type.elementType().assertAssignableTo(JType.charT)
        index.type.assertAssignableTo(JType.intT)
        return entry(JType.charT, ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceCastore(instruction: Castore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType().assertAssignableTo(JType.charT)
        index.type.assertAssignableTo(JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceCheckcast(instruction: Checkcast, objectref: StackEntry<Expression>): StackEntry<Expression> {
        objectref.type.assertReference()
        val type = (instruction.indexbyte as Constant<JType>).value
        return entry(type, CastExpr(Decompiler.convertType(type), objectref.value))
    }

    override fun reduceD2f(instruction: D2f, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.doubleT)
        return entry(JType.floatT, CastExpr(PrimitiveType.floatType(), value.value))
    }

    override fun reduceD2i(instruction: D2i, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.doubleT)
        return entry(JType.floatT, CastExpr(PrimitiveType.intType(), value.value))
    }

    override fun reduceD2l(instruction: D2l, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.doubleT)
        return entry(JType.floatT, CastExpr(PrimitiveType.longType(), value.value))
    }

    override fun reduceDadd(instruction: Dadd, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.doubleT)
        value2.type.assertAssignableTo(JType.doubleT)
        return entry(JType.doubleT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.PLUS))
    }

    override fun reduceDaload(instruction: Daload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        arrayref.type.elementType().assertAssignableTo(JType.doubleT)
        index.type.assertAssignableTo(JType.intT)
        return entry(JType.doubleT, ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceDastore(instruction: Dastore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType().assertAssignableTo(JType.doubleT)
        index.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceDcmpg(instruction: Dcmpg, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        return entry(JType.intT, MethodCallExpr(NameExpr("Double"), "compareTo", NodeList(value1.value, value2.value)))
    }

    override fun reduceDcmpl(instruction: Dcmpl, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        // NaN is negative infinity not positive infinity
        return entry(JType.intT, UnaryExpr(MethodCallExpr(NameExpr("Double"), "compareTo", NodeList(value2.value, value1.value)), UnaryExpr.Operator.MINUS))
    }

    override fun reduceDconst_0(instruction: Dconst_0): StackEntry<Expression> {
        return entry(JType.doubleT, DoubleLiteralExpr(0.0))
    }

    override fun reduceDconst_1(instruction: Dconst_1): StackEntry<Expression> {
        return entry(JType.doubleT, DoubleLiteralExpr(1.0))
    }

    override fun reduceDdiv(instruction: Ddiv, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        return entry(JType.doubleT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE))
    }

    override fun reduceDload(instruction: Dload): StackEntry<Expression> {
        context.getOrMakeLocal(instruction.index!!, JType.doubleT)
        return entry(JType.doubleT, NameExpr("v" + instruction.index))
    }

    override fun reduceDload_0(instruction: Dload_0): StackEntry<Expression> {
        context.getOrMakeLocal(0, JType.doubleT)
        return entry(JType.doubleT, NameExpr("v0"))
    }

    override fun reduceDload_1(instruction: Dload_1): StackEntry<Expression> {
        context.getOrMakeLocal(1, JType.doubleT)
        return entry(JType.doubleT, NameExpr("v1"))
    }

    override fun reduceDload_2(instruction: Dload_2): StackEntry<Expression> {
        context.getOrMakeLocal(2, JType.doubleT)
        return entry(JType.doubleT, NameExpr("v2"))
    }

    override fun reduceDload_3(instruction: Dload_3): StackEntry<Expression> {
        context.getOrMakeLocal(3, JType.doubleT)
        return entry(JType.doubleT, NameExpr("v3"))
    }

    override fun reduceDmul(instruction: Dmul, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        return entry(JType.doubleT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY))
    }

    override fun reduceDneg(instruction: Dneg, value: StackEntry<Expression>): StackEntry<Expression> {
        return entry(JType.doubleT, UnaryExpr(value.value, UnaryExpr.Operator.MINUS))
    }

    override fun reduceDrem(instruction: Drem, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        return entry(JType.doubleT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER))
    }

    override fun reduceDreturn(instruction: Dreturn, value: StackEntry<Expression>) {
        emitter.invoke(ReturnStmt(value.value))
    }

    override fun reduceDstore(instruction: Dstore, value: StackEntry<Expression>) {
        context.getOrMakeLocal(instruction.index!!, JType.doubleT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceDstore_0(instruction: Dstore_0, value: StackEntry<Expression>) {
        context.getOrMakeLocal(0, JType.doubleT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceDstore_1(instruction: Dstore_1, value: StackEntry<Expression>) {
        context.getOrMakeLocal(1, JType.doubleT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceDstore_2(instruction: Dstore_2, value: StackEntry<Expression>) {
        context.getOrMakeLocal(2, JType.doubleT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceDstore_3(instruction: Dstore_3, value: StackEntry<Expression>) {
        context.getOrMakeLocal(3, JType.doubleT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceDsub(instruction: Dsub, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.doubleT)
        value2.type.assertAssignableTo(JType.doubleT)
        return entry(JType.doubleT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS))
    }

    override fun reduceDup(instruction: Dup, value: StackEntry<Expression>): Pair<StackEntry<Expression>, StackEntry<Expression>> {
        return value to value
    }

    override fun reduceDup_x1(instruction: Dup_x1, value2: StackEntry<Expression>, value1: StackEntry<Expression>): Triple<StackEntry<Expression>, StackEntry<Expression>, StackEntry<Expression>> {
        return Triple(value1, value2, value1)
    }

    override fun reduceDup_x2(instruction: Dup_x2, value1: StackEntry<Expression>, value2: StackEntry<Expression>, value3: StackEntry<Expression>?): ImmutableList<StackEntry<Expression>> {
        value1.type.assertComputationType(1)
        return if (value2.type.computationType == 1) {
            ImmutableList.of(value1, value3!!, value2, value1)
        } else {
            ImmutableList.of(value1, value2, value1)
        }
    }

    override fun reduceDup2(instruction: Dup2, value1: StackEntry<Expression>, value2: StackEntry<Expression>?): ImmutableList<StackEntry<Expression>> {
        return if (value1.type.computationType == 1) {
            ImmutableList.of(value1, value2!!, value1, value2!!)
        } else {
            ImmutableList.of(value1, value1)
        }
    }

    override fun reduceDup2_x1(instruction: Dup2_x1, value1: StackEntry<Expression>, value2: StackEntry<Expression>, value3: StackEntry<Expression>?): ImmutableList<StackEntry<Expression>> {
        throw UnsupportedOperationException("Dup2_x1")
    }

    override fun reduceDup2_x2(instruction: Dup2_x2, value1: StackEntry<Expression>, value2: StackEntry<Expression>, value3: StackEntry<Expression>?, value4: StackEntry<Expression>?): ImmutableList<StackEntry<Expression>> {
        throw UnsupportedOperationException("Dup2_x2")
    }

    override fun reduceF2d(instruction: F2d, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.floatT)
        return entry(JType.doubleT, CastExpr(PrimitiveType.doubleType(), value.value))
    }

    override fun reduceF2i(instruction: F2i, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.floatT)
        return entry(JType.intT, CastExpr(PrimitiveType.intType(), value.value))
    }

    override fun reduceF2l(instruction: F2l, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.floatT)
        return entry(JType.longT, CastExpr(PrimitiveType.longType(), value.value))
    }

    override fun reduceFadd(instruction: Fadd, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.floatT)
        value2.type.assertAssignableTo(JType.floatT)
        return entry(JType.floatT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.PLUS))
    }

    override fun reduceFaload(instruction: Faload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        arrayref.type.elementType().assertAssignableTo(JType.floatT)
        index.type.assertAssignableTo(JType.intT)
        return entry(JType.floatT, ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceFastore(instruction: Fastore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType().assertAssignableTo(JType.floatT)
        index.type.assertAssignableTo(JType.intT)
        value.type.assertAssignableTo(JType.floatT)
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceFcmpg(instruction: Fcmpg, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.floatT)
        value2.type.assertAssignableTo(JType.floatT)
        return entry(JType.intT, MethodCallExpr(NameExpr("Float"), "compareTo", NodeList(value1.value, value2.value)))
    }

    override fun reduceFcmpl(instruction: Fcmpl, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        // NaN is negative infinity not positive infinity
        value2.type.assertAssignableTo(JType.floatT)
        value1.type.assertAssignableTo(JType.floatT)
        return entry(JType.intT, UnaryExpr(MethodCallExpr(NameExpr("Float"), "compareTo", NodeList(value2.value, value1.value)), UnaryExpr.Operator.MINUS))
    }

    override fun reduceFconst_0(instruction: Fconst_0): StackEntry<Expression> {
        return entry(JType.floatT, DoubleLiteralExpr(0.0)) // TODO: we should somehow make this codegen as 0F not 0D
    }

    override fun reduceFconst_1(instruction: Fconst_1): StackEntry<Expression> {
        return entry(JType.floatT, DoubleLiteralExpr(1.0))
    }

    override fun reduceFconst_2(instruction: Fconst_2): StackEntry<Expression> {
        return entry(JType.floatT, DoubleLiteralExpr(2.0))
    }

    override fun reduceFdiv(instruction: Fdiv, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.floatT)
        value2.type.assertAssignableTo(JType.floatT)
        return entry(JType.floatT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE))
    }

    override fun reduceFload(instruction: Fload): StackEntry<Expression> {
        context.getOrMakeLocal(instruction.index!!, JType.floatT)
        return entry(JType.floatT, NameExpr("v" + instruction.index))
    }

    override fun reduceFload_0(instruction: Fload_0): StackEntry<Expression> {
        context.getOrMakeLocal(0, JType.floatT)
        return entry(JType.floatT, NameExpr("v0"))
    }

    override fun reduceFload_1(instruction: Fload_1): StackEntry<Expression> {
        context.getOrMakeLocal(1, JType.floatT)
        return entry(JType.floatT, NameExpr("v1"))
    }

    override fun reduceFload_2(instruction: Fload_2): StackEntry<Expression> {
        context.getOrMakeLocal(2, JType.floatT)
        return entry(JType.floatT, NameExpr("v2"))
    }

    override fun reduceFload_3(instruction: Fload_3): StackEntry<Expression> {
        context.getOrMakeLocal(3, JType.floatT)
        return entry(JType.floatT, NameExpr("v3"))
    }

    override fun reduceFmul(instruction: Fmul, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.floatT)
        value2.type.assertAssignableTo(JType.floatT)
        return entry(JType.floatT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY))
    }

    override fun reduceFneg(instruction: Fneg, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.floatT)
        return entry(JType.floatT, UnaryExpr(value.value, UnaryExpr.Operator.MINUS))
    }

    override fun reduceFrem(instruction: Frem, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.floatT)
        value2.type.assertAssignableTo(JType.floatT)
        return entry(JType.floatT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER))
    }

    override fun reduceFreturn(instruction: Freturn, value: StackEntry<Expression>) {
        value.type.assertAssignableTo(JType.floatT)
        emitter.invoke(ReturnStmt(value.value))
    }

    override fun reduceFstore(instruction: Fstore, value: StackEntry<Expression>) {
        context.getOrMakeLocal(instruction.index!!, JType.floatT)
        value.type.assertAssignableTo(JType.floatT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceFstore_0(instruction: Fstore_0, value: StackEntry<Expression>) {
        context.getOrMakeLocal(0, JType.floatT)
        value.type.assertAssignableTo(JType.floatT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceFstore_1(instruction: Fstore_1, value: StackEntry<Expression>) {
        context.getOrMakeLocal(1, JType.floatT)
        value.type.assertAssignableTo(JType.floatT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceFstore_2(instruction: Fstore_2, value: StackEntry<Expression>) {
        context.getOrMakeLocal(2, JType.floatT)
        value.type.assertAssignableTo(JType.floatT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceFstore_3(instruction: Fstore_3, value: StackEntry<Expression>) {
        context.getOrMakeLocal(3, JType.floatT)
        value.type.assertAssignableTo(JType.floatT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceFsub(instruction: Fsub, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.floatT)
        value2.type.assertAssignableTo(JType.floatT)
        return entry(JType.floatT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS))
    }

    override fun reduceGetfield(instruction: Getfield, objectref: StackEntry<Expression>): StackEntry<Expression> {
        val field = (instruction.indexbyte as Constant<Field>).value
        return entry(field.type, FieldAccessExpr(objectref.value, field.name))
    }

    override fun reduceGetstatic(instruction: Getstatic): StackEntry<Expression> {
        val field = (instruction.indexbyte as Constant<Field>).value
        return entry(field.type, FieldAccessExpr(TypeExpr(ClassOrInterfaceType(field.parent.name)), field.name))
    }

    override fun reduceGoto(instruction: Goto) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceGoto_w(instruction: Goto_w) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceI2b(instruction: I2b, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.intT)
        return entry(JType.byteT, CastExpr(PrimitiveType.byteType(), value.value))
    }

    override fun reduceI2c(instruction: I2c, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.intT)
        return entry(JType.charT, CastExpr(PrimitiveType.charType(), value.value))
    }

    override fun reduceI2d(instruction: I2d, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.intT)
        return entry(JType.doubleT, CastExpr(PrimitiveType.doubleType(), value.value))
    }

    override fun reduceI2f(instruction: I2f, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.intT)
        return entry(JType.floatT, CastExpr(PrimitiveType.floatType(), value.value))
    }

    override fun reduceI2l(instruction: I2l, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.intT)
        return entry(JType.longT, CastExpr(PrimitiveType.longType(), value.value))
    }

    override fun reduceI2s(instruction: I2s, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.intT)
        return entry(JType.shortT, CastExpr(PrimitiveType.shortType(), value.value))
    }

    override fun reduceIadd(instruction: Iadd, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS))
    }

    override fun reduceIaload(instruction: Iaload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        arrayref.type.elementType().assertAssignableTo(JType.intT)
        index.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceIand(instruction: Iand, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_AND))
    }

    override fun reduceIastore(instruction: Iastore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType().assertAssignableTo(JType.intT)
        index.type.assertAssignableTo(JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceIconst_m1(instruction: Iconst_m1): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(-1))
    }

    override fun reduceIconst_0(instruction: Iconst_0): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(0))
    }

    override fun reduceIconst_1(instruction: Iconst_1): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(1))
    }

    override fun reduceIconst_2(instruction: Iconst_2): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(2))
    }

    override fun reduceIconst_3(instruction: Iconst_3): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(3))
    }

    override fun reduceIconst_4(instruction: Iconst_4): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(4))
    }

    override fun reduceIconst_5(instruction: Iconst_5): StackEntry<Expression> {
        return entry(JType.intT, IntegerLiteralExpr(5))
    }

    override fun reduceIdiv(instruction: Idiv, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE))
    }

    override fun reduceIf_acmpeq(instruction: If_acmpeq, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIf_acmpne(instruction: If_acmpne, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIf_icmpeq(instruction: If_icmpeq, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIf_icmpne(instruction: If_icmpne, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIf_icmplt(instruction: If_icmplt, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIf_icmpge(instruction: If_icmpge, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIf_icmpgt(instruction: If_icmpgt, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIf_icmple(instruction: If_icmple, value1: StackEntry<Expression>, value2: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIfeq(instruction: Ifeq, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIfne(instruction: Ifne, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIflt(instruction: Iflt, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIfge(instruction: Ifge, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIfgt(instruction: Ifgt, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIfle(instruction: Ifle, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIfnonnull(instruction: Ifnonnull, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIfnull(instruction: Ifnull, value: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceIinc(instruction: Iinc) {
        // TODO: this is unsafe, must be inlined probably
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v" + instruction.index), IntegerLiteralExpr(instruction.const_!!), AssignExpr.Operator.PLUS)))
    }

    override fun reduceIload(instruction: Iload): StackEntry<Expression> {
        context.getOrMakeLocal(instruction.index!!, JType.intT)
        return entry(JType.intT, NameExpr("v" + instruction.index))
    }

    override fun reduceIload_0(instruction: Iload_0): StackEntry<Expression> {
        context.getOrMakeLocal(0, JType.intT)
        return entry(JType.intT, NameExpr("v0"))
    }

    override fun reduceIload_1(instruction: Iload_1): StackEntry<Expression> {
        context.getOrMakeLocal(1, JType.intT)
        return entry(JType.intT, NameExpr("v1"))
    }

    override fun reduceIload_2(instruction: Iload_2): StackEntry<Expression> {
        context.getOrMakeLocal(2, JType.intT)
        return entry(JType.intT, NameExpr("v2"))
    }

    override fun reduceIload_3(instruction: Iload_3): StackEntry<Expression> {
        context.getOrMakeLocal(3, JType.intT)
        return entry(JType.intT, NameExpr("v3"))
    }

    override fun reduceImul(instruction: Imul, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY))
    }

    override fun reduceIneg(instruction: Ineg, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, UnaryExpr(value.value, UnaryExpr.Operator.MINUS))
    }

    override fun reduceInstanceof(instruction: Instanceof, objectref: StackEntry<Expression>): StackEntry<Expression> {
        return entry(JType.booleanT, InstanceOfExpr(objectref.value, Decompiler.convertType((instruction.indexbyte as Constant<JType>).value) as ReferenceType))
    }

    override fun reduceIor(instruction: Ior, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_OR))
    }

    override fun reduceIrem(instruction: Irem, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER))
    }

    override fun reduceIreturn(instruction: Ireturn, value: StackEntry<Expression>) {
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ReturnStmt(value.value))
    }

    override fun reduceIshl(instruction: Ishl, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LEFT_SHIFT))
    }

    override fun reduceIshr(instruction: Ishr, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.SIGNED_RIGHT_SHIFT))
    }

    override fun reduceIstore(instruction: Istore, value: StackEntry<Expression>) {
        context.getOrMakeLocal(instruction.index!!, JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceIstore_0(instruction: Istore_0, value: StackEntry<Expression>) {
        context.getOrMakeLocal(0, JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceIstore_1(instruction: Istore_1, value: StackEntry<Expression>) {
        context.getOrMakeLocal(1, JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceIstore_2(instruction: Istore_2, value: StackEntry<Expression>) {
        context.getOrMakeLocal(2, JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceIstore_3(instruction: Istore_3, value: StackEntry<Expression>) {
        context.getOrMakeLocal(3, JType.intT)
        value.type.assertAssignableTo(JType.intT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceIsub(instruction: Isub, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS))
    }

    override fun reduceIushr(instruction: Iushr, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT))
    }

    override fun reduceIxor(instruction: Ixor, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.intT)
        value2.type.assertAssignableTo(JType.intT)
        return entry(JType.intT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.XOR))
    }

    override fun reduceJsr(instruction: Jsr): StackEntry<Expression> {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceJsr_w(instruction: Jsr_w): StackEntry<Expression> {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceL2d(instruction: L2d, value: StackEntry<Expression>): StackEntry<Expression> {
        return value
    }

    override fun reduceL2f(instruction: L2f, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.longT)
        return entry(JType.floatT, CastExpr(PrimitiveType.floatType(), value.value))
    }

    override fun reduceL2i(instruction: L2i, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.longT)
        return entry(JType.intT, CastExpr(PrimitiveType.intType(), value.value))
    }

    override fun reduceLadd(instruction: Ladd, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.PLUS))
    }

    override fun reduceLaload(instruction: Laload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        arrayref.type.elementType().assertAssignableTo(JType.longT)
        index.type.assertAssignableTo(JType.intT)
        return entry(JType.longT, ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceLand(instruction: Land, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_AND))
    }

    override fun reduceLastore(instruction: Lastore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType().assertAssignableTo(JType.longT)
        index.type.assertAssignableTo(JType.intT)
        value.type.assertAssignableTo(JType.longT)
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceLcmp(instruction: Lcmp, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.intT, MethodCallExpr(NameExpr("Long"), "compareTo", NodeList(value1.value, value2.value)))
    }

    override fun reduceLconst_0(instruction: Lconst_0): StackEntry<Expression> {
        return entry(JType.longT, LongLiteralExpr(0L))
    }

    override fun reduceLconst_1(instruction: Lconst_1): StackEntry<Expression> {
        return entry(JType.longT, LongLiteralExpr(-1L))
    }

    private fun loadConstant(constant: Constant<*>): StackEntry<Expression> {
        return when {
            constant.value is Byte -> entry(JType.byteT, IntegerLiteralExpr(constant.value.toInt()))
            constant.value is Short -> entry(JType.shortT, IntegerLiteralExpr(constant.value.toInt()))
            constant.value is Int -> entry(JType.intT, IntegerLiteralExpr(constant.value))
            constant.value is Long -> entry(JType.longT, LongLiteralExpr(constant.value))
            constant.value is Float -> entry(JType.floatT, DoubleLiteralExpr(constant.value.toDouble()))
            constant.value is Double -> entry(JType.doubleT, DoubleLiteralExpr(constant.value))
            constant.value is String -> entry(JType.instance(classpath.loadKlass("java/lang/String")), StringLiteralExpr(constant.value))
            constant.value is JType -> entry(JType.instance(classpath.loadKlass("java/lang/Class")), FieldAccessExpr(TypeExpr(Decompiler.convertType(constant.value)), "class"))
            else -> throw UnsupportedOperationException("Unexpected literal type loaded: " + constant.value!!.javaClass.simpleName)
        }
    }

    override fun reduceLdc(instruction: Ldc): StackEntry<Expression> {
        return loadConstant(instruction.index!!)
    }

    override fun reduceLdc_w(instruction: Ldc_w): StackEntry<Expression> {
        return loadConstant(instruction.indexbyte!!)
    }

    override fun reduceLdc2_w(instruction: Ldc2_w): StackEntry<Expression> {
        return loadConstant(instruction.indexbyte!!)
    }

    override fun reduceLdiv(instruction: Ldiv, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.DIVIDE))
    }

    override fun reduceLload(instruction: Lload): StackEntry<Expression> {
        context.getOrMakeLocal(instruction.index!!, JType.longT)
        return entry(JType.longT, NameExpr("v" + instruction.index))
    }

    override fun reduceLload_0(instruction: Lload_0): StackEntry<Expression> {
        context.getOrMakeLocal(0, JType.longT)
        return entry(JType.longT, NameExpr("v0"))
    }

    override fun reduceLload_1(instruction: Lload_1): StackEntry<Expression> {
        context.getOrMakeLocal(1, JType.longT)
        return entry(JType.longT, NameExpr("v1"))
    }

    override fun reduceLload_2(instruction: Lload_2): StackEntry<Expression> {
        context.getOrMakeLocal(2, JType.longT)
        return entry(JType.longT, NameExpr("v2"))
    }

    override fun reduceLload_3(instruction: Lload_3): StackEntry<Expression> {
        context.getOrMakeLocal(3, JType.longT)
        return entry(JType.longT, NameExpr("v3"))
    }

    override fun reduceLmul(instruction: Lmul, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MULTIPLY))
    }

    override fun reduceLneg(instruction: Lneg, value: StackEntry<Expression>): StackEntry<Expression> {
        value.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, UnaryExpr(value.value, UnaryExpr.Operator.MINUS))
    }

    override fun reduceLor(instruction: Lor, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.BINARY_OR))
    }

    override fun reduceLrem(instruction: Lrem, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.REMAINDER))
    }

    override fun reduceLreturn(instruction: Lreturn, value: StackEntry<Expression>) {
        value.type.assertAssignableTo(JType.longT)
        emitter.invoke(ReturnStmt(value.value))
    }

    override fun reduceLshl(instruction: Lshl, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.LEFT_SHIFT))
    }

    override fun reduceLshr(instruction: Lshr, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.SIGNED_RIGHT_SHIFT))
    }

    override fun reduceLstore(instruction: Lstore, value: StackEntry<Expression>) {
        context.getOrMakeLocal(instruction.index!!, JType.longT)
        value.type.assertAssignableTo(JType.longT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v" + instruction.index), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceLstore_0(instruction: Lstore_0, value: StackEntry<Expression>) {
        context.getOrMakeLocal(0, JType.longT)
        value.type.assertAssignableTo(JType.longT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v0"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceLstore_1(instruction: Lstore_1, value: StackEntry<Expression>) {
        context.getOrMakeLocal(1, JType.longT)
        value.type.assertAssignableTo(JType.longT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v1"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceLstore_2(instruction: Lstore_2, value: StackEntry<Expression>) {
        context.getOrMakeLocal(2, JType.longT)
        value.type.assertAssignableTo(JType.longT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v2"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceLstore_3(instruction: Lstore_3, value: StackEntry<Expression>) {
        context.getOrMakeLocal(3, JType.longT)
        value.type.assertAssignableTo(JType.longT)
        emitter.invoke(ExpressionStmt(AssignExpr(NameExpr("v3"), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceLsub(instruction: Lsub, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.MINUS))
    }

    override fun reduceLushr(instruction: Lushr, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT))
    }

    override fun reduceLxor(instruction: Lxor, value1: StackEntry<Expression>, value2: StackEntry<Expression>): StackEntry<Expression> {
        value1.type.assertAssignableTo(JType.longT)
        value2.type.assertAssignableTo(JType.longT)
        return entry(JType.longT, BinaryExpr(value1.value, value2.value, BinaryExpr.Operator.XOR))
    }

    override fun reduceMonitorenter(instruction: Monitorenter, objectref: StackEntry<Expression>) {
        throw RuntimeException("should only be run in basic blocks, monitorenter variant")
    }

    override fun reduceMonitorexit(instruction: Monitorexit, objectref: StackEntry<Expression>) {
        throw RuntimeException("should only be run in basic blocks, monitorexit variant")
    }

    override fun reduceMultianewarray(instruction: Multianewarray, count: List<StackEntry<Expression>>): StackEntry<Expression> {
        val type = (instruction.indexbyte as Constant<JType>).value
        val levels = count.stream().map { expr ->
            expr.type.assertAssignableTo(JType.intT)
            ArrayCreationLevel(expr.value)
        }.collect(Collectors.toList<ArrayCreationLevel>() as Collector<in ArrayCreationLevel, Any, List<ArrayCreationLevel>>?)
        return entry(JType.array(type), ArrayCreationExpr(Decompiler.convertType(type), NodeList(levels), null))
    }

    override fun reduceNew(instruction: New): StackEntry<Expression> {
        val type = (instruction.indexbyte as Constant<JType>).value
        return entry(type, ObjectCreationExpr(null, Decompiler.convertType(type) as ClassOrInterfaceType, NodeList()))
    }

    override fun reduceNewarray(instruction: Newarray, count: StackEntry<Expression>): StackEntry<Expression> {
        count.type.assertAssignableTo(JType.intT)
        val arrayType = JType.array(instruction.atype!!.type)
        return entry(arrayType, ArrayCreationExpr(Decompiler.convertType(arrayType), NodeList(ArrayCreationLevel(count.value)), null))
    }

    override fun reduceNop(instruction: Nop) {
        // nop
    }

    override fun reducePop(instruction: Pop, value: StackEntry<Expression>) {
        emitter.invoke(ExpressionStmt(value.value))
    }

    override fun reducePop2(instruction: Pop2, value1: StackEntry<Expression>, value2: StackEntry<Expression>?) {
        emitter.invoke(ExpressionStmt(value1.value))
        if (value2 != null) {
            emitter.invoke(ExpressionStmt(value2!!.value))
        }
    }

    override fun reducePutfield(instruction: Putfield, objectref: StackEntry<Expression>, value: StackEntry<Expression>) {
        val field = (instruction.indexbyte as Constant<Field>).value
        objectref.type.assertReference()
        value.type.assertAssignableTo(field.type)
        emitter.invoke(ExpressionStmt(AssignExpr(FieldAccessExpr(objectref.value, field.name), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reducePutstatic(instruction: Putstatic, value: StackEntry<Expression>) {
        val field = (instruction.indexbyte as Constant<Field>).value
        value.type.assertAssignableTo(field.type)
        emitter.invoke(ExpressionStmt(AssignExpr(FieldAccessExpr(TypeExpr(Decompiler.convertType(JType.instance(field.parent))), field.name), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceRet(instruction: Ret) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceReturn(instruction: Return) {
        emitter.invoke(ReturnStmt())
    }

    override fun reduceSaload(instruction: Saload, arrayref: StackEntry<Expression>, index: StackEntry<Expression>): StackEntry<Expression> {
        arrayref.type.elementType().assertAssignableTo(JType.shortT)
        index.type.assertAssignableTo(JType.intT)
        return entry(JType.shortT, ArrayAccessExpr(arrayref.value, index.value))
    }

    override fun reduceSastore(instruction: Sastore, arrayref: StackEntry<Expression>, index: StackEntry<Expression>, value: StackEntry<Expression>) {
        arrayref.type.elementType().assertAssignableTo(JType.shortT)
        index.type.assertAssignableTo(JType.intT)
        value.type.assertAssignableTo(JType.shortT)
        emitter.invoke(ExpressionStmt(AssignExpr(ArrayAccessExpr(arrayref.value, index.value), value.value, AssignExpr.Operator.ASSIGN)))
    }

    override fun reduceSipush(instruction: Sipush): StackEntry<Expression> {
        return entry(JType.shortT, IntegerLiteralExpr(instruction.byte_!!))
    }

    override fun reduceSwap(instruction: Swap, value2: StackEntry<Expression>, value1: StackEntry<Expression>): Pair<StackEntry<Expression>, StackEntry<Expression>> {
        //TODO: execution order is messed up here no doubt
        return value1 to value2
    }

    private fun instanceInvokation(objectref: StackEntry<Expression>, arguments: List<StackEntry<Expression>>, method: Method): StackEntry<Expression>? {
        val expr = MethodCallExpr(objectref.value, method.name, NodeList(arguments.stream().map { entry -> entry.value }.collect(Collectors.toList<Expression>() as Collector<in Expression, Any, List<Expression>>?)))
        return if (method.descriptor.returnType === JType.voidT) {
            emitter.invoke(ExpressionStmt(expr))
            null
        } else {
            entry(method.descriptor.returnType, expr)
        }
    }

    override fun reduceInvokeinterface(instruction: Invokeinterface, objectref: StackEntry<Expression>, arguments: List<StackEntry<Expression>>): StackEntry<Expression>? {
        return instanceInvokation(objectref, arguments, (instruction.indexbyte as Constant<Method>).value)
    }

    override fun reduceInvokestatic(instruction: Invokestatic, arguments: List<StackEntry<Expression>>): StackEntry<Expression>? {
        val method = (instruction.indexbyte as Constant<Method>).value
        val expr = MethodCallExpr(TypeExpr(Decompiler.convertType(JType.instance(method.parent))), method.name, NodeList(arguments.stream().map { entry -> entry.value }.collect(Collectors.toList<Expression>() as Collector<in Expression, Any, List<Expression>>?)))
        return if (method.descriptor.returnType === JType.voidT) {
            emitter.invoke(ExpressionStmt(expr))
            null
        } else {
            entry(method.descriptor.returnType, expr)
        }
    }

    override fun reduceInvokevirtual(instruction: Invokevirtual, objectref: StackEntry<Expression>, arguments: List<StackEntry<Expression>>): StackEntry<Expression>? {
        return instanceInvokation(objectref, arguments, (instruction.indexbyte as Constant<Method>).value)
    }

    override fun reduceInvokedynamic(instruction: Invokedynamic, arguments: List<StackEntry<Expression>>): StackEntry<Expression>? {
        val dynamic = (instruction.indexbyte as Constant<Dynamic>).value
        val descriptor = dynamic.nameAndType.type.right().fromJust()
        //     public MethodReferenceExpr(Expression scope, NodeList<Type> typeArguments, String identifier) {
        //        this((TokenRange)null, scope, typeArguments, identifier);
        //
        val subHandle = (dynamic.bootstrapMethod.arguments[1] as Constant<MethodHandle>).value
        val type = subHandle.refType
        val scope: Expression
        val handleName: String
        if (type == MethodHandle.MethodHandleType.REF_getField || type == MethodHandle.MethodHandleType.REF_putField) {
            //TODO: this logic is a hack
            scope = if (arguments.isEmpty())
                TypeExpr(Decompiler.convertType(JType.instance((subHandle.ref as Constant<Field>).value.parent)))
            else
                arguments[0].value
            handleName = (subHandle.ref as Constant<Field>).value.name
        } else if (type == MethodHandle.MethodHandleType.REF_invokeVirtual ||
                type == MethodHandle.MethodHandleType.REF_invokeSpecial ||
                type == MethodHandle.MethodHandleType.REF_newInvokeSpecial ||
                type == MethodHandle.MethodHandleType.REF_invokeInterface) {
            //TODO: this logic is a hack
            scope = if (arguments.isEmpty())
                TypeExpr(Decompiler.convertType(JType.instance((subHandle.ref as Constant<Method>).value.parent)))
            else
                arguments[0].value
            handleName = (subHandle.ref as Constant<Method>).value.name
        } else if (type == MethodHandle.MethodHandleType.REF_getStatic || type == MethodHandle.MethodHandleType.REF_putStatic) {
            scope = TypeExpr(Decompiler.convertType(JType.instance((subHandle.ref as Constant<Field>).value.parent)))
            handleName = subHandle.ref.value.name
        } else if (type == MethodHandle.MethodHandleType.REF_invokeStatic) {
            scope = TypeExpr(Decompiler.convertType(JType.instance((subHandle.ref as Constant<Method>).value.parent)))
            handleName = subHandle.ref.value.name
        } else {
            throw RuntimeException("Unexpected method handle type: $type")
        }
        val expr = MethodReferenceExpr(scope, NodeList(), handleName)
        return entry(descriptor.returnType, expr)
    }

    override fun reduceInvokespecial(instruction: Invokespecial, objectref: StackEntry<Expression>, arguments: List<StackEntry<Expression>>): StackEntry<Expression>? {
        val method = (instruction.indexbyte as Constant<Method>).value
        return if (objectref.value is ObjectCreationExpr) {
            if (method.descriptor.returnType !== JType.voidT) {
                throw RuntimeException("unexpected return type from invokespecial: " + method.descriptor.toString())
            }
            objectref.value.arguments = NodeList(arguments.stream().map { x -> x.value }.collect(Collectors.toList<Expression>() as Collector<in Expression, Any, List<Expression>>?))
            null
        } else {
            instanceInvokation(objectref, arguments, method)
        }
    }

    override fun reduceLookupswitch(instruction: Lookupswitch, index: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceTableswitch(instruction: Tableswitch, index: StackEntry<Expression>) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceEnterTry(instruction: EnterTry) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceExitTry(instruction: ExitTry) {
        throw RuntimeException("should only run on basic blocks")
    }

    override fun reduceLabel(instruction: Label) {
        throw RuntimeException("should only run on basic blocks")
    }
}
