package com.javaprophet.jasm.bytecode;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import com.javaprophet.jasm.ClassFile;
import com.javaprophet.jasm.constant.CClass;
import com.javaprophet.jasm.constant.CDouble;
import com.javaprophet.jasm.constant.CFieldRef;
import com.javaprophet.jasm.constant.CFloat;
import com.javaprophet.jasm.constant.CInteger;
import com.javaprophet.jasm.constant.CInterfaceMethodRef;
import com.javaprophet.jasm.constant.CInvokeDynamic;
import com.javaprophet.jasm.constant.CLong;
import com.javaprophet.jasm.constant.CMethodHandle;
import com.javaprophet.jasm.constant.CMethodRef;
import com.javaprophet.jasm.constant.CMethodType;
import com.javaprophet.jasm.constant.CNameAndType;
import com.javaprophet.jasm.constant.CString;
import com.javaprophet.jasm.constant.CUTF8;
import com.javaprophet.jasm.constant.ConstantInfo;

public class InstructionSet {
	private final ClassFile cf;
	private final String name;
	
	public InstructionSet(String name, ClassFile cf) {
		this.cf = cf;
		this.name = name;
	}
	
	public byte[] code = null;
	
	public InstructionSet read(byte[] code) {
		this.code = code;
		return this;
	}
	
	private static final String crlf = System.getProperty("line.separator");
	
	public String resolve(int cref) {
		return (String)resolve(cref, true);
	}
	
	private Object resolve(int cref, boolean base) {
		ConstantInfo ci = cf.getConstant(cref);
		Object res = null;
		if (ci instanceof CClass) {
			res = resolve(((CClass)ci).name_index, false);
		}else if (ci instanceof CDouble) {
			res = ((CDouble)ci).dbl + "";
		}else if (ci instanceof CLong) {
			res = ((CLong)ci).lng + "";
		}else if (ci instanceof CInteger) {
			res = ((CInteger)ci).integer + "";
		}else if (ci instanceof CFloat) {
			res = ((CFloat)ci).flt + "";
		}else if (ci instanceof CFieldRef) {
			res = resolve(((CFieldRef)ci).class_index, false) + "/" + (String)resolve(((CFieldRef)ci).name_and_type_index, false);
		}else if (ci instanceof CMethodRef) {
			res = resolve(((CMethodRef)ci).class_index, false) + "/" + (String)resolve(((CMethodRef)ci).name_and_type_index, false);
		}else if (ci instanceof CInterfaceMethodRef) {
			res = resolve(((CInterfaceMethodRef)ci).class_index, false) + "/" + (String)resolve(((CInterfaceMethodRef)ci).name_and_type_index, false);
		}else if (ci instanceof CInvokeDynamic) {
			res = resolve(((CInvokeDynamic)ci).name_and_type_index, false);
		}else if (ci instanceof CMethodHandle) {
			res = resolve(((CMethodHandle)ci).reference_index, false); // TODO: reference_type
		}else if (ci instanceof CMethodType) {
			res = resolve(((CMethodType)ci).descriptor_index, false);
		}else if (ci instanceof CNameAndType) {
			res = resolve(((CNameAndType)ci).name_index, false) + " " + (String)resolve(((CNameAndType)ci).descriptor_index, false); // TODO: reference_type
		}else if (ci instanceof CString) {
			res = resolve(((CString)ci).string_index, false);
		}else if (ci instanceof CUTF8) {
			res = ((CUTF8)ci).utf;
		}
		if (base) {
			String ress = (String)res;
			ress = cref + " // -> " + ress;
			ress = ress.replace(crlf, crlf + "//");
			res = ress;
		}
		return res;
	}
	
	public String resolveold(int cref) {
		ConstantInfo ci = cf.getConstant(cref);
		String ev = "";
		if (ci instanceof CInteger) {
			ev = ((CInteger)ci).integer + "";
		}else if (ci instanceof CFloat) {
			ev = ((CFloat)ci).flt + "";
		}else if (ci instanceof CString) {
			int ri = ((CString)ci).string_index;
			CUTF8 cu = (CUTF8)cf.getConstant(ri);
			ev = ri + " // -> " + cu.utf;
		}else if (ci instanceof CClass) {
			int ri = ((CClass)ci).name_index;
			CUTF8 cu = (CUTF8)cf.getConstant(ri);
			ev = ri + " // -> " + cu.utf;
		}else if (ci instanceof CMethodType) {
			int ri = ((CMethodType)ci).descriptor_index;
			CUTF8 cu = (CUTF8)cf.getConstant(ri);
			ev = ri + " // -> " + cu.utf;
		}else if (ci instanceof CMethodHandle) {
			int ri = ((CMethodHandle)ci).reference_index; // TODO: switch of ref index
			CUTF8 cu = (CUTF8)cf.getConstant(ri);
			ev = ri + " // -> " + cu.utf;
		}else if (ci instanceof CLong) {
			ev = ((CLong)ci).lng + "";
		}else if (ci instanceof CDouble) {
			ev = ((CDouble)ci).dbl + "";
		}else if (ci instanceof CMethodRef) {
			CClass ci2 = (CClass)cf.getConstant(((CMethodRef)ci).class_index);
			CNameAndType ci3 = (CNameAndType)cf.getConstant(((CMethodRef)ci).name_and_type_index);
			int ri = ci2.name_index;
			CUTF8 cu = (CUTF8)cf.getConstant(ri);
			ev = ri + " // -> " + cu.utf;
		}else if (ci instanceof CInterfaceMethodRef) {
			ci = (CClass)cf.getConstant(((CInterfaceMethodRef)ci).class_index);
			int ri = ((CClass)ci).name_index;
			CUTF8 cu = (CUTF8)cf.getConstant(ri);
			ev = ri + " // -> " + cu.utf;
		}else if (ci instanceof CFieldRef) {
			ci = (CClass)cf.getConstant(((CFieldRef)ci).class_index);
			int ri = ((CClass)ci).name_index;
			CUTF8 cu = (CUTF8)cf.getConstant(ri);
			ev = ri + " // -> " + cu.utf;
		}
		ev = ev.replace(crlf, crlf + "//");
		return ev;
	}
	
	public String toString() {
		// if (!name.equals("replace")) return "";
		StringWriter sb = new StringWriter();
		PrintWriter pw = new PrintWriter(sb);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(code));
		try {
			while (in.available() > 0) {
				int bc = in.read();
				switch (bc) {
				case 0:// nop
					pw.println("nop");
					break;
				case 1:
					pw.println("aconst_null");
					break;
				case 2:
					pw.println("iconst_m1");
					break;
				case 3:
					pw.println("iconst_0");
					break;
				case 4:
					pw.println("iconst_1");
					break;
				case 5:
					pw.println("iconst_2");
					break;
				case 6:
					pw.println("iconst_3");
					break;
				case 7:
					pw.println("iconst_4");
					break;
				case 8:
					pw.println("iconst_5");
					break;
				case 9:
					pw.println("lconst_0");
					break;
				case 10:
					pw.println("lconst_1");
					break;
				case 11:
					pw.println("fconst_0");
					break;
				case 12:
					pw.println("fconst_1");
					break;
				case 13:
					pw.println("fconst_2");
					break;
				case 14:
					pw.println("dconst_0");
					break;
				case 15:
					pw.println("dconst_1");
					break;
				case 16:
					pw.println("bipush " + in.read());
					break;
				case 17:
					pw.println("sipush");
					break;
				case 18:
					pw.println("ldc " + resolve(in.read()));
					break;
				case 19:
					pw.println("ldc_w " + resolve(in.readUnsignedShort()));
					break;
				case 20:
					pw.println("ldc2_w " + resolve(in.readUnsignedShort()));
					break;
				case 21:
					pw.println("iload " + in.read());
					break;
				case 22:
					pw.println("lload " + in.read());
					break;
				case 23:
					pw.println("fload " + in.read());
					break;
				case 24:
					pw.println("dload " + in.read());
					break;
				case 25:
					pw.println("aload " + in.read());
					break;
				case 26:
					pw.println("iload_0");
					break;
				case 27:
					pw.println("iload_1");
					break;
				case 28:
					pw.println("iload_2");
					break;
				case 29:
					pw.println("iload_3");
					break;
				case 30:
					pw.println("lload_0");
					break;
				case 31:
					pw.println("lload_1");
					break;
				case 32:
					pw.println("lload_2");
					break;
				case 33:
					pw.println("lload_3");
					break;
				case 34:
					pw.println("fload_0");
					break;
				case 35:
					pw.println("fload_1");
					break;
				case 36:
					pw.println("fload_2");
					break;
				case 37:
					pw.println("fload_3");
					break;
				case 38:
					pw.println("dload_0");
					break;
				case 39:
					pw.println("dload_1");
					break;
				case 40:
					pw.println("dload_2");
					break;
				case 41:
					pw.println("dload_3");
					break;
				case 42:
					pw.println("aload_0");
					break;
				case 43:
					pw.println("aload_1");
					break;
				case 44:
					pw.println("aload_2");
					break;
				case 45:
					pw.println("aload_3");
					break;
				case 46:
					pw.println("iaload");
					break;
				case 47:
					pw.println("laload");
					break;
				case 48:
					pw.println("faload");
					break;
				case 49:
					pw.println("daload");
					break;
				case 50:
					pw.println("aaload");
					break;
				case 51:
					pw.println("baload");
					break;
				case 52:
					pw.println("caload");
					break;
				case 53:
					pw.println("saload");
					break;
				case 54:
					pw.println("istore " + in.read());
					break;
				case 55:
					pw.println("lstore " + in.read());
					break;
				case 56:
					pw.println("fstore " + in.read());
					break;
				case 57:
					pw.println("dstore " + in.read());
					break;
				case 58:
					pw.println("astore " + in.read());
					break;
				case 59:
					pw.println("istore_0");
					break;
				case 60:
					pw.println("istore_1");
					break;
				case 61:
					pw.println("istore_2");
					break;
				case 62:
					pw.println("istore_3");
					break;
				case 63:
					pw.println("lstore_0");
					break;
				case 64:
					pw.println("lstore_1");
					break;
				case 65:
					pw.println("lstore_2");
					break;
				case 66:
					pw.println("lstore_3");
					break;
				case 67:
					pw.println("fstore_0");
					break;
				case 68:
					pw.println("fstore_1");
					break;
				case 69:
					pw.println("fstore_2");
					break;
				case 70:
					pw.println("fstore_3");
					break;
				case 71:
					pw.println("dstore_0");
					break;
				case 72:
					pw.println("dstore_1");
					break;
				case 73:
					pw.println("dstore_2");
					break;
				case 74:
					pw.println("dstore_3");
					break;
				case 75:
					pw.println("astore_0");
					break;
				case 76:
					pw.println("astore_1");
					break;
				case 77:
					pw.println("astore_2");
					break;
				case 78:
					pw.println("astore_3");
					break;
				case 79:
					pw.println("iastore");
					break;
				case 80:
					pw.println("lastore");
					break;
				case 81:
					pw.println("fastore");
					break;
				case 82:
					pw.println("dastore");
					break;
				case 83:
					pw.println("aastore");
					break;
				case 84:
					pw.println("bastore");
					break;
				case 85:
					pw.println("castore");
					break;
				case 86:
					pw.println("sastore");
					break;
				case 87:
					pw.println("pop");
					break;
				case 88:
					pw.println("pop2");
					break;
				case 89:
					pw.println("dup");
					break;
				case 90:
					pw.println("dup_x1");
					break;
				case 91:
					pw.println("dup_x2");
					break;
				case 92:
					pw.println("dup2");
					break;
				case 93:
					pw.println("dup2_x1");
					break;
				case 94:
					pw.println("dup2_x2");
					break;
				case 95:
					pw.println("swap");
					break;
				case 96:
					pw.println("iadd");
					break;
				case 97:
					pw.println("ladd");
					break;
				case 98:
					pw.println("fadd");
					break;
				case 99:
					pw.println("dadd");
					break;
				case 100:
					pw.println("isub");
					break;
				case 101:
					pw.println("lsub");
					break;
				case 102:
					pw.println("fsub");
					break;
				case 103:
					pw.println("dsub");
					break;
				case 104:
					pw.println("imul");
					break;
				case 105:
					pw.println("lmul");
					break;
				case 106:
					pw.println("fmul");
					break;
				case 107:
					pw.println("dmul");
					break;
				case 108:
					pw.println("idiv");
					break;
				case 109:
					pw.println("ldiv");
					break;
				case 110:
					pw.println("fdiv");
					break;
				case 111:
					pw.println("ddiv");
					break;
				case 112:
					pw.println("irem");
					break;
				case 113:
					pw.println("lrem");
					break;
				case 114:
					pw.println("frem");
					break;
				case 115:
					pw.println("drem");
					break;
				case 116:
					pw.println("ineg");
					break;
				case 117:
					pw.println("lneg");
					break;
				case 118:
					pw.println("fneg");
					break;
				case 119:
					pw.println("dneg");
					break;
				case 120:
					pw.println("ishl");
					break;
				case 121:
					pw.println("lshl");
					break;
				case 122:
					pw.println("ishr");
					break;
				case 123:
					pw.println("lshr");
					break;
				case 124:
					pw.println("iushr");
					break;
				case 125:
					pw.println("lushr");
					break;
				case 126:
					pw.println("iand");
					break;
				case 127:
					pw.println("land");
					break;
				case 128:
					pw.println("ior");
					break;
				case 129:
					pw.println("lor");
					break;
				case 130:
					pw.println("ixor");
					break;
				case 131:
					pw.println("lxor");
					break;
				case 132:
					pw.println("iinc " + in.read() + " " + in.read());
					break;
				case 133:
					pw.println("i2l");
					break;
				case 134:
					pw.println("i2f");
					break;
				case 135:
					pw.println("i2d");
					break;
				case 136:
					pw.println("l2i");
					break;
				case 137:
					pw.println("l2f");
					break;
				case 138:
					pw.println("l2d");
					break;
				case 139:
					pw.println("f2i");
					break;
				case 140:
					pw.println("f2l");
					break;
				case 141:
					pw.println("f2d");
					break;
				case 142:
					pw.println("d2i");
					break;
				case 143:
					pw.println("d2l");
					break;
				case 144:
					pw.println("d2f");
					break;
				case 145:
					pw.println("i2b");
					break;
				case 146:
					pw.println("i2c");
					break;
				case 147:
					pw.println("i2s");
					break;
				case 148:
					pw.println("lcmp");
					break;
				case 149:
					pw.println("fcmpl");
					break;
				case 150:
					pw.println("fcmpg");
					break;
				case 151:
					pw.println("dcmpl");
					break;
				case 152:
					pw.println("dcmpg");
					break;
				case 153:
					pw.println("ifeq " + in.readShort());
					break;
				case 154:
					pw.println("ifne " + in.readShort());
					break;
				case 155:
					pw.println("iflt " + in.readShort());
					break;
				case 156:
					pw.println("ifge " + in.readShort());
					break;
				case 157:
					pw.println("ifgt " + in.readShort());
					break;
				case 158:
					pw.println("ifle " + in.readShort());
					break;
				case 159:
					pw.println("if_icmpeq " + in.readShort());
					break;
				case 160:
					pw.println("if_icmpne " + in.readShort());
					break;
				case 161:
					pw.println("if_icmplt " + in.readShort());
					break;
				case 162:
					pw.println("if_icmpge " + in.readShort());
					break;
				case 163:
					pw.println("if_icmpgt " + in.readShort());
					break;
				case 164:
					pw.println("if_icmple " + in.readShort());
					break;
				case 165:
					pw.println("if_acmpeq " + in.readShort());
					break;
				case 166:
					pw.println("if_acmpne " + in.readShort());
					break;
				case 167:
					pw.println("goto " + in.readUnsignedShort());
					break;
				case 168:
					pw.println("jsr " + in.readUnsignedShort());
					break;
				case 169:
					pw.println("ret");
					break;
				case 170:
					pw.println("tableswitch");
					break;
				case 171:
					pw.println("lookupswitch");
					// TODO: make
					break;
				case 172:
					pw.println("ireturn");
					break;
				case 173:
					pw.println("lreturn");
					break;
				case 174:
					pw.println("freturn");
					break;
				case 175:
					pw.println("dreturn");
					break;
				case 176:
					pw.println("areturn");
					break;
				case 177:
					pw.println("return");
					break;
				case 178:
					pw.println("getstatic " + resolve(in.readUnsignedShort()));
					break;
				case 179:
					pw.println("putstatic " + resolve(in.readUnsignedShort()));
					break;
				case 180:
					pw.println("getfield " + resolve(in.readUnsignedShort()));
					break;
				case 181:
					pw.println("putfield " + resolve(in.readUnsignedShort()));
					break;
				case 182:
					pw.println("invokevirtual " + resolve(in.readUnsignedShort()));
					break;
				case 183:
					pw.println("invokespecial " + resolve(in.readUnsignedShort()));
					break;
				case 184:
					pw.println("invokestatic " + resolve(in.readUnsignedShort()));
					break;
				case 185:
					pw.println("invokeinterface " + resolve(in.readUnsignedShort()) + " " + in.read());
					break;
				case 186:
					pw.println("invokedynamic " + resolve(in.readUnsignedShort()));
					break;
				case 187:
					pw.println("new " + resolve(in.readUnsignedShort()));
					break;
				case 188:
					int t = in.read();
					String r = "";
					switch (t) {
					case 4:
						r = "T_BOOLEAN";
						break;
					case 5:
						r = "T_CHAR";
						break;
					case 6:
						r = "T_FLOAT";
						break;
					case 7:
						r = "T_DOUBLE";
						break;
					case 8:
						r = "T_BYTE";
						break;
					case 9:
						r = "T_SHORT";
						break;
					case 10:
						r = "T_INT";
						break;
					case 11:
						r = "T_LONG";
						break;
					default:
						r = t + "";
						break;
					}
					pw.println("newarray " + r);
					break;
				case 189:
					pw.println("anewarray " + resolve(in.readUnsignedShort()));
					break;
				case 190:
					pw.println("arraylength");
					break;
				case 191:
					pw.println("athrow");
					break;
				case 192:
					pw.println("checkcast " + resolve(in.readUnsignedShort()));
					break;
				case 193:
					pw.println("instanceof " + resolve(in.readUnsignedShort()));
					break;
				case 194:
					pw.println("monitorenter");
					break;
				case 195:
					pw.println("monitorexit");
					break;
				case 196:
					pw.println("wide");
					break;
				case 197:
					pw.println("multianewarray " + resolve(in.readUnsignedShort()) + " " + in.read());
					break;
				case 198:
					pw.println("ifnull " + in.readUnsignedShort());
					break;
				case 199:
					pw.println("ifnonnull " + in.readUnsignedShort());
					break;
				case 200:
					pw.println("goto_w " + in.readInt());
					break;
				case 201:
					pw.println("jsr_w " + in.readInt());
					break;
				case 202:
					pw.println("breakpoint");
					break;
				case 254:
					pw.println("impdep1");
					break;
				case 255:
					pw.println("impdep2");
					break;
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
			pw.println("// IO ERROR!!!!");
		}
		return sb.toString();
	}
}
