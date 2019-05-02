package com.protryon.jasm;

import com.protryon.jasm.instruction.StackDirector;
import com.protryon.jasm.instruction.StackReducer;
import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.Tuple3;
import com.shapesecurity.functional.data.Maybe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJASM {

    private static class TestReducer extends StackReducer<String> {

        private final Consumer<String> logger;

        public TestReducer(Consumer<String> logger) {
            this.logger = logger;
        }

        @Override
        public String reduceAaload(Aaload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public void reduceAastore(Aastore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceAconst_null(Aconst_null instruction) {
            return "null";
        }

        @Override
        public String reduceAload(Aload instruction) {
            return "local<" + instruction.index.index + ">";
        }

        @Override
        public String reduceAload_0(Aload_0 instruction) {
            return "local<0>";
        }

        @Override
        public String reduceAload_1(Aload_1 instruction) {
            return "local<1>";
        }

        @Override
        public String reduceAload_2(Aload_2 instruction) {
            return "local<2>";
        }

        @Override
        public String reduceAload_3(Aload_3 instruction) {
            return "local<3>";
        }

        @Override
        public String reduceAnewarray(Anewarray instruction, String count) {
            return "new " + ((Constant<Klass>) instruction.indexbyte).value.name + "[" + count + "]";
        }

        @Override
        public void reduceAreturn(Areturn instruction, String objectref) {

        }

        @Override
        public String reduceArraylength(Arraylength instruction, String arrayref) {
            return arrayref + ".length";
        }

        @Override
        public void reduceAstore(Astore instruction, String objectref) {

        }

        @Override
        public void reduceAstore_0(Astore_0 instruction, String objectref) {

        }

        @Override
        public void reduceAstore_1(Astore_1 instruction, String objectref) {

        }

        @Override
        public void reduceAstore_2(Astore_2 instruction, String objectref) {

        }

        @Override
        public void reduceAstore_3(Astore_3 instruction, String objectref) {

        }

        @Override
        public String reduceAthrow(Athrow instruction, String objectref) {
            return "throw " + objectref;
        }

        @Override
        public String reduceBaload(Baload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public void reduceBastore(Bastore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceBipush(Bipush instruction) {
            return instruction.byte_ + "";
        }

        @Override
        public String reduceCaload(Caload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public void reduceCastore(Castore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceCheckcast(Checkcast instruction, String objectref) {
            return objectref; // TODO: show cast?
        }

        @Override
        public String reduceD2f(D2f instruction, String value) {
            return null;
        }

        @Override
        public String reduceD2i(D2i instruction, String value) {
            return value;
        }

        @Override
        public String reduceD2l(D2l instruction, String value) {
            return value;
        }

        @Override
        public String reduceDadd(Dadd instruction, String value1, String value2) {
            return value2 + " + " + value2;
        }

        @Override
        public String reduceDaload(Daload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public void reduceDastore(Dastore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceDcmpg(Dcmpg instruction, String value1, String value2) {
            return "dcmpg(" + value1 + ", " + value2 + ")";
        }

        @Override
        public String reduceDcmpl(Dcmpl instruction, String value1, String value2) {
            return "dcmpl(" + value1 + ", " + value2 + ")";
        }

        @Override
        public String reduceDconst_0(Dconst_0 instruction) {
            return "0d";
        }

        @Override
        public String reduceDconst_1(Dconst_1 instruction) {
            return "1d";
        }

        @Override
        public String reduceDdiv(Ddiv instruction, String value1, String value2) {
            return value1 + " / " + value2;
        }

        @Override
        public String reduceDload(Dload instruction) {
            return "local<" + instruction.index + ">";
        }

        @Override
        public String reduceDload_0(Dload_0 instruction) {
            return "local<0>";
        }

        @Override
        public String reduceDload_1(Dload_1 instruction) {
            return "local<1>";
        }

        @Override
        public String reduceDload_2(Dload_2 instruction) {
            return "local<2>";
        }

        @Override
        public String reduceDload_3(Dload_3 instruction) {
            return "local<3>";
        }

        @Override
        public String reduceDmul(Dmul instruction, String value1, String value2) {
            return value1 + " * " + value2;
        }

        @Override
        public String reduceDneg(Dneg instruction, String value) {
            return "-" + value;
        }

        @Override
        public String reduceDrem(Drem instruction, String value1, String value2) {
            return value1 + " % " + value2;
        }

        @Override
        public void reduceDreturn(Dreturn instruction, String value) {

        }

        @Override
        public void reduceDstore(Dstore instruction, String value) {

        }

        @Override
        public void reduceDstore_0(Dstore_0 instruction, String value) {

        }

        @Override
        public void reduceDstore_1(Dstore_1 instruction, String value) {

        }

        @Override
        public void reduceDstore_2(Dstore_2 instruction, String value) {

        }

        @Override
        public void reduceDstore_3(Dstore_3 instruction, String value) {

        }

        @Override
        public String reduceDsub(Dsub instruction, String value1, String value2) {
            return value1 + " - " + value2;
        }

        @Override
        public Pair<String, String> reduceDup(Dup instruction, String value) {
            return Pair.of(value, value);
        }

        @Override
        public Tuple3<String, String, String> reduceDup_x1(Dup_x1 instruction, String value2, String value1) {
            return Tuple3.of(value1, value2, value1);
        }

        @Override
        public void reduceDup_x2(Dup_x2 instruction) {
            throw new UnsupportedOperationException("dup_x2");
        }

        @Override
        public void reduceDup2(Dup2 instruction) {
            throw new UnsupportedOperationException("dup2");
        }

        @Override
        public void reduceDup2_x1(Dup2_x1 instruction) {
            throw new UnsupportedOperationException("dup2_x1");
        }

        @Override
        public void reduceDup2_x2(Dup2_x2 instruction) {
            throw new UnsupportedOperationException("dup2_x2");
        }

        @Override
        public String reduceF2d(F2d instruction, String value) {
            return value;
        }

        @Override
        public String reduceF2i(F2i instruction, String value) {
            return value;
        }

        @Override
        public String reduceF2l(F2l instruction, String value) {
            return value;
        }

        @Override
        public String reduceFadd(Fadd instruction, String value1, String value2) {
            return value1 + " + " + value2;
        }

        @Override
        public String reduceFaload(Faload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public void reduceFastore(Fastore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceFcmpg(Fcmpg instruction, String value1, String value2) {
            return "fcmpg(" + value1 + ", " + value2 + ")";
        }

        @Override
        public String reduceFcmpl(Fcmpl instruction, String value1, String value2) {
            return "fcmpl(" + value1 + ", " + value2 + ")";
        }

        @Override
        public String reduceFconst_0(Fconst_0 instruction) {
            return "0f";
        }

        @Override
        public String reduceFconst_1(Fconst_1 instruction) {
            return "1f";
        }

        @Override
        public String reduceFconst_2(Fconst_2 instruction) {
            return "2f";
        }

        @Override
        public String reduceFdiv(Fdiv instruction, String value1, String value2) {
            return value1 + " / " + value2;
        }

        @Override
        public String reduceFload(Fload instruction) {
            return "local<" + instruction.index.index + ">";
        }

        @Override
        public String reduceFload_0(Fload_0 instruction) {
            return "local<0>";
        }

        @Override
        public String reduceFload_1(Fload_1 instruction) {
            return "local<1>";
        }

        @Override
        public String reduceFload_2(Fload_2 instruction) {
            return "local<2>";
        }

        @Override
        public String reduceFload_3(Fload_3 instruction) {
            return "local<3>";
        }

        @Override
        public String reduceFmul(Fmul instruction, String value1, String value2) {
            return value1 + " * " + value2;
        }

        @Override
        public String reduceFneg(Fneg instruction, String value) {
            return "-" + value;
        }

        @Override
        public String reduceFrem(Frem instruction, String value1, String value2) {
            return value1 + " % " + value2;
        }

        @Override
        public void reduceFreturn(Freturn instruction, String value) {

        }

        @Override
        public void reduceFstore(Fstore instruction, String value) {

        }

        @Override
        public void reduceFstore_0(Fstore_0 instruction, String value) {

        }

        @Override
        public void reduceFstore_1(Fstore_1 instruction, String value) {

        }

        @Override
        public void reduceFstore_2(Fstore_2 instruction, String value) {

        }

        @Override
        public void reduceFstore_3(Fstore_3 instruction, String value) {

        }

        @Override
        public String reduceFsub(Fsub instruction, String value1, String value2) {
            return value1 + " - " + value2;
        }

        @Override
        public String reduceGetfield(Getfield instruction, String objectref) {
            return objectref + "." + ((Constant<Field>) instruction.indexbyte).value.name;
        }

        @Override
        public String reduceGetstatic(Getstatic instruction) {
            Field field = ((Constant<Field>) instruction.indexbyte).value;
            return field.parent.name + "." + field.name;
        }

        @Override
        public void reduceGoto(Goto instruction) {

        }

        @Override
        public void reduceGoto_w(Goto_w instruction) {

        }

        @Override
        public String reduceI2b(I2b instruction, String value) {
            return value;
        }

        @Override
        public String reduceI2c(I2c instruction, String value) {
            return value;
        }

        @Override
        public String reduceI2d(I2d instruction, String value) {
            return value;
        }

        @Override
        public String reduceI2f(I2f instruction, String value) {
            return value;
        }

        @Override
        public String reduceI2l(I2l instruction, String value) {
            return value;
        }

        @Override
        public String reduceI2s(I2s instruction, String value) {
            return value;
        }

        @Override
        public String reduceIadd(Iadd instruction, String value1, String value2) {
            return value1 + " + " + value2;
        }

        @Override
        public String reduceIaload(Iaload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public String reduceIand(Iand instruction, String value1, String value2) {
            return value1 + " & " + value2;
        }

        @Override
        public void reduceIastore(Iastore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceIconst_m1(Iconst_m1 instruction) {
            return "-1";
        }

        @Override
        public String reduceIconst_0(Iconst_0 instruction) {
            return "0";
        }

        @Override
        public String reduceIconst_1(Iconst_1 instruction) {
            return "1";
        }

        @Override
        public String reduceIconst_2(Iconst_2 instruction) {
            return "2";
        }

        @Override
        public String reduceIconst_3(Iconst_3 instruction) {
            return "3";
        }

        @Override
        public String reduceIconst_4(Iconst_4 instruction) {
            return "4";
        }

        @Override
        public String reduceIconst_5(Iconst_5 instruction) {
            return "5";
        }

        @Override
        public String reduceIdiv(Idiv instruction, String value1, String value2) {
            return value1 + " / " + value2;
        }

        @Override
        public void reduceIf_acmpeq(If_acmpeq instruction, String value1, String value2) {

        }

        @Override
        public void reduceIf_acmpne(If_acmpne instruction, String value1, String value2) {

        }

        @Override
        public void reduceIf_icmpeq(If_icmpeq instruction, String value1, String value2) {

        }

        @Override
        public void reduceIf_icmpne(If_icmpne instruction, String value1, String value2) {

        }

        @Override
        public void reduceIf_icmplt(If_icmplt instruction, String value1, String value2) {

        }

        @Override
        public void reduceIf_icmpge(If_icmpge instruction, String value1, String value2) {

        }

        @Override
        public void reduceIf_icmpgt(If_icmpgt instruction, String value1, String value2) {

        }

        @Override
        public void reduceIf_icmple(If_icmple instruction, String value1, String value2) {

        }

        @Override
        public void reduceIfeq(Ifeq instruction, String value) {

        }

        @Override
        public void reduceIfne(Ifne instruction, String value) {

        }

        @Override
        public void reduceIflt(Iflt instruction, String value) {

        }

        @Override
        public void reduceIfge(Ifge instruction, String value) {

        }

        @Override
        public void reduceIfgt(Ifgt instruction, String value) {

        }

        @Override
        public void reduceIfle(Ifle instruction, String value) {

        }

        @Override
        public void reduceIfnonnull(Ifnonnull instruction, String value) {

        }

        @Override
        public void reduceIfnull(Ifnull instruction, String value) {

        }

        @Override
        public void reduceIinc(Iinc instruction) {

        }

        @Override
        public String reduceIload(Iload instruction) {
            return "local<" + instruction.index.index + ">";
        }

        @Override
        public String reduceIload_0(Iload_0 instruction) {
            return "local<0>";
        }

        @Override
        public String reduceIload_1(Iload_1 instruction) {
            return "local<1>";
        }

        @Override
        public String reduceIload_2(Iload_2 instruction) {
            return "local<2>";
        }

        @Override
        public String reduceIload_3(Iload_3 instruction) {
            return "local<3>";
        }

        @Override
        public String reduceImul(Imul instruction, String value1, String value2) {
            return value1 + " * " + value2;
        }

        @Override
        public String reduceIneg(Ineg instruction, String value) {
            return "-" + value;
        }

        @Override
        public String reduceInstanceof(Instanceof instruction, String objectref) {
            return objectref + " instanceof " + ((Constant<Klass>) instruction.indexbyte).value.name;
        }

        @Override
        public String reduceIor(Ior instruction, String value1, String value2) {
            return value1 + " | " + value2;
        }

        @Override
        public String reduceIrem(Irem instruction, String value1, String value2) {
            return value1 + " % " + value2;
        }

        @Override
        public void reduceIreturn(Ireturn instruction, String value) {

        }

        @Override
        public String reduceIshl(Ishl instruction, String value1, String value2) {
            return value1 + " << " + value2;
        }

        @Override
        public String reduceIshr(Ishr instruction, String value1, String value2) {
            return value1 + " >> " + value2;
        }

        @Override
        public void reduceIstore(Istore instruction, String value) {

        }

        @Override
        public void reduceIstore_0(Istore_0 instruction, String value) {

        }

        @Override
        public void reduceIstore_1(Istore_1 instruction, String value) {

        }

        @Override
        public void reduceIstore_2(Istore_2 instruction, String value) {

        }

        @Override
        public void reduceIstore_3(Istore_3 instruction, String value) {

        }

        @Override
        public String reduceIsub(Isub instruction, String value1, String value2) {
            return value1 + " - " + value2;
        }

        @Override
        public String reduceIushr(Iushr instruction, String value1, String value2) {
            return value1 + " >>> " + value2;
        }

        @Override
        public String reduceIxor(Ixor instruction, String value1, String value2) {
            return value1 + " ^ " + value2;
        }

        @Override
        public String reduceJsr(Jsr instruction) {
            return "jump return location";
        }

        @Override
        public String reduceJsr_w(Jsr_w instruction) {
            return "jump return location wide";
        }

        @Override
        public String reduceL2d(L2d instruction, String value) {
            return value;
        }

        @Override
        public String reduceL2f(L2f instruction, String value) {
            return value;
        }

        @Override
        public String reduceL2i(L2i instruction, String value) {
            return value;
        }

        @Override
        public String reduceLadd(Ladd instruction, String value1, String value2) {
            return value1 + " + " + value2;
        }

        @Override
        public String reduceLaload(Laload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public String reduceLand(Land instruction, String value1, String value2) {
            return value1 + " & " + value2;
        }

        @Override
        public void reduceLastore(Lastore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceLcmp(Lcmp instruction, String value1, String value2) {
            return "lcmp(" + value1 + ", " + value2 + ")";
        }

        @Override
        public String reduceLconst_0(Lconst_0 instruction) {
            return "0l";
        }

        @Override
        public String reduceLconst_1(Lconst_1 instruction) {
            return "-1l";
        }

        @Override
        public String reduceLdc(Ldc instruction) {
            return instruction.index.toString();
        }

        @Override
        public String reduceLdc_w(Ldc_w instruction) {
            return instruction.indexbyte.toString();
        }

        @Override
        public String reduceLdc2_w(Ldc2_w instruction) {
            return instruction.indexbyte.toString();
        }

        @Override
        public String reduceLdiv(Ldiv instruction, String value1, String value2) {
            return value1 + " / " + value2;
        }

        @Override
        public String reduceLload(Lload instruction) {
            return "local<" + instruction.index.index + ">";
        }

        @Override
        public String reduceLload_0(Lload_0 instruction) {
            return "local<0>";
        }

        @Override
        public String reduceLload_1(Lload_1 instruction) {
            return "local<1>";
        }

        @Override
        public String reduceLload_2(Lload_2 instruction) {
            return "local<2>";
        }

        @Override
        public String reduceLload_3(Lload_3 instruction) {
            return "local<3>";
        }

        @Override
        public String reduceLmul(Lmul instruction, String value1, String value2) {
            return value1 + " * " + value2;
        }

        @Override
        public String reduceLneg(Lneg instruction, String value) {
            return "-" + value;
        }

        @Override
        public String reduceLor(Lor instruction, String value1, String value2) {
            return value1 + " | " + value2;
        }

        @Override
        public String reduceLrem(Lrem instruction, String value1, String value2) {
            return value1 + " % " + value2;
        }

        @Override
        public void reduceLreturn(Lreturn instruction, String value) {

        }

        @Override
        public String reduceLshl(Lshl instruction, String value1, String value2) {
            return value1 + " << " + value2;
        }

        @Override
        public String reduceLshr(Lshr instruction, String value1, String value2) {
            return value1 + " >> " + value2;
        }

        @Override
        public void reduceLstore(Lstore instruction, String value) {

        }

        @Override
        public void reduceLstore_0(Lstore_0 instruction, String value) {

        }

        @Override
        public void reduceLstore_1(Lstore_1 instruction, String value) {

        }

        @Override
        public void reduceLstore_2(Lstore_2 instruction, String value) {

        }

        @Override
        public void reduceLstore_3(Lstore_3 instruction, String value) {

        }

        @Override
        public String reduceLsub(Lsub instruction, String value1, String value2) {
            return value1 + " - " + value2;
        }

        @Override
        public String reduceLushr(Lushr instruction, String value1, String value2) {
            return value1 + " >>> " + value2;
        }

        @Override
        public String reduceLxor(Lxor instruction, String value1, String value2) {
            return value1 + " ^ " + value2;
        }

        @Override
        public void reduceMonitorenter(Monitorenter instruction, String objectref) {

        }

        @Override
        public void reduceMonitorexit(Monitorexit instruction, String objectref) {

        }

        @Override
        public String reduceMultianewarray(Multianewarray instruction, List<String> dimensions) {
            return "new " + ((Constant<Klass>) instruction.indexbyte).value.name + dimensions.stream().map(dimension -> "[" + dimension + "]").collect(Collectors.joining());
        }

        @Override
        public String reduceNew(New instruction) {
            return "new " + ((Constant<Klass>) instruction.indexbyte).value.name;
        }

        @Override
        public String reduceNewarray(Newarray instruction, String count) {
            return "new " + instruction.atype.type.javaName + "[" + count + "]";
        }

        @Override
        public void reduceNop(Nop instruction) {

        }

        @Override
        public void reducePop(Pop instruction, String value) {

        }

        @Override
        public void reducePop2(Pop2 instruction) {

        }

        @Override
        public void reducePutfield(Putfield instruction, String objectref, String value) {

        }

        @Override
        public void reducePutstatic(Putstatic instruction, String value) {
            logger.accept(((Constant<Field>) instruction.indexbyte).value.name + " = " + value);
        }

        @Override
        public void reduceRet(Ret instruction) {

        }

        @Override
        public void reduceReturn(Return instruction) {

        }

        @Override
        public String reduceSaload(Saload instruction, String arrayref, String index) {
            return arrayref + "[" + index + "]";
        }

        @Override
        public void reduceSastore(Sastore instruction, String arrayref, String index, String value) {

        }

        @Override
        public String reduceSipush(Sipush instruction) {
            return instruction.byte_ + "";
        }

        @Override
        public Pair<String, String> reduceSwap(Swap instruction, String value2, String value1) {
            return Pair.of(value1, value2);
        }

        @Override
        public Maybe<String> reduceInvokeinterface(Invokeinterface instruction, String objectref, List<String> arguments) {
            Method method = ((Constant<Method>) instruction.indexbyte).value;
            if (method.descriptor.returnType != JType.voidT) {
                return Maybe.of(objectref + "." + method.name + "(" + arguments.stream().collect(Collectors.joining(", ")) + ")");
            } else {
                return Maybe.empty();
            }
        }

        @Override
        public Maybe<String> reduceInvokestatic(Invokestatic instruction, List<String> arguments) {
            Method method = ((Constant<Method>) instruction.indexbyte).value;
            if (method.descriptor.returnType != JType.voidT) {
                return Maybe.of(method.parent.name + "." + method.name + "(" + arguments.stream().collect(Collectors.joining(", ")) + ")");
            } else {
                return Maybe.empty();
            }
        }

        @Override
        public Maybe<String> reduceInvokevirtual(Invokevirtual instruction, String objectref, List<String> arguments) {
            Method method = ((Constant<Method>) instruction.indexbyte).value;
            if (method.descriptor.returnType != JType.voidT) {
                return Maybe.of(objectref + "." + method.name + "(" + arguments.stream().collect(Collectors.joining(", ")) + ")");
            } else {
                return Maybe.empty();
            }
        }

        @Override
        public Maybe<String> reduceInvokespecial(Invokespecial instruction, String objectref, List<String> arguments) {
            Method method = ((Constant<Method>) instruction.indexbyte).value;
            if (method.descriptor.returnType != JType.voidT) {
                return Maybe.of(objectref + "." + method.name + "(" + arguments.stream().collect(Collectors.joining(", ")) + ")");
            } else {
                return Maybe.empty();
            }
        }

        @Override
        public void reduceLookupswitch(Lookupswitch instruction, String index) {

        }

        @Override
        public void reduceTableswitch(Tableswitch instruction, String index) {

        }

        @Override
        public void reduceEnterTry(EnterTry instruction) {

        }

        @Override
        public void reduceExitTry(ExitTry instruction) {

        }

        @Override
        public void reduceLabel(Label instruction) {

        }
    }

    @Test
    public void testJASM() throws IOException {

        // this is all local testing stuff for Minecraft that cannot legally be pushed to GitHub AFAIK

        // if you wish to run this test, download minecraft and modify this as you please

        // i will probably write better tests for this in the future......

        Classpath classpath = new Classpath("/p/git/Burger/1.14.jar");

        Klass bmm = classpath.lookupKlass("bmm");

        Method clinit = bmm.methods.get(1);

        System.out.println(clinit.toString());

        assertEquals(0, StackDirector.reduceInstructions(new TestReducer(System.out::println), clinit.code, new LinkedList<>()).size());

        assertNotNull(bmm);

        //System.out.println(bmm);

    }
}
