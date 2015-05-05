package com.javaprophet.jasm.bytecode;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import com.javaprophet.jasm.ClassFile;

public class InstructionSet {
	private final ClassFile cf;
	
	public InstructionSet(ClassFile cf) {
		this.cf = cf;
	}
	
	public byte[] code = null;
	
	public InstructionSet read(byte[] code) {
		this.code = code;
		return this;
	}
	
	public byte[] write() {
		return this.code;
	}
	
	public void fromString(String s) {
		Scanner in = new Scanner(s);
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line.contains("//")) line = line.substring(0, line.indexOf("//"));
			line = line.trim();
			if (line.length() == 0) continue;
			String[] args = line.split(" ");
			if (args.length == 0) continue;
			
		}
	}
	
	private static final String[] opcodes = new String[]{//
	"nop", //
			"aconst_null", //
			"iconst_m1", //
			"iconst_0", //
			"iconst_1", //
			"iconst_2", //
			"iconst_3", //
			"iconst_4", //
			"iconst_5", //
			"lconst_0", //
			"lconst_1", //
			"fconst_0", //
			"fconst_1", //
			"fconst_2", //
			"dconst_0", //
			"dconst_1", //
			"bipush", //
			"sipush", //
			"ldc", //
			"ldc_w", //
			"ldc2_w", //
			"iload", //
			"lload", //
			"fload", //
			"dload", //
			"aload", //
			"iload_0", //
			"iload_1", //
			"iload_2", //
			"iload_3", //
			"lload_0", //
			"lload_1", //
			"lload_2", //
			"lload_3", //
			"fload_0", //
			"fload_1", //
			"fload_2", //
			"fload_3", //
			"dload_0", //
			"dload_1", //
			"dload_2", //
			"dload_3", //
			"aload_0", //
			"aload_1", //
			"aload_2", //
			"aload_3", //
			"iaload", //
			"laload", //
			"faload", //
			"daload", //
			"aaload", //
			"baload", //
			"caload", //
			"saload", //
			"istore", //
			"lstore", //
			"fstore", //
			"dstore", //
			"astore", //
			"istore_0", //
			"istore_1", //
			"istore_2", //
			"istore_3", //
			"lstore_0", //
			"lstore_1", //
			"lstore_2", //
			"lstore_3", //
			"fstore_0", //
			"fstore_1", //
			"fstore_2", //
			"fstore_3", //
			"dstore_0", //
			"dstore_1", //
			"dstore_2", //
			"dstore_3", //
			"astore_0", //
			"astore_1", //
			"astore_2", //
			"astore_3", //
			"iastore", //
			"lastore", //
			"fastore", //
			"dastore", //
			"aastore", //
			"bastore", //
			"castore", //
			"sastore", //
			"pop", //
			"pop2", //
			"dup", //
			"dup_x1", //
			"dup_x2", //
			"dup2", //
			"dup2_x1", //
			"dup2_x2", //
			"swap", //
			"iadd", //
			"ladd", //
			"fadd", //
			"dadd", //
			"isub", //
			"lsub", //
			"fsub", //
			"dsub", //
			"imul", //
			"lmul", //
			"fmul", //
			"dmul", //
			"idiv", //
			"ldiv", //
			"fdiv", //
			"ddiv", //
			"irem", //
			"lrem", //
			"frem", //
			"drem", //
			"ineg", //
			"lneg", //
			"fneg", //
			"dneg", //
			"ishl", //
			"lshl", //
			"ishr", //
			"lshr", //
			"iushr", //
			"lushr", //
			"iand", //
			"land", //
			"ior", //
			"lor", //
			"ixor", //
			"lxor", //
			"iinc", //
			"i2l", //
			"i2f", //
			"i2d", //
			"l2i", //
			"l2f", //
			"l2d", //
			"f2i", //
			"f2l", //
			"f2d", //
			"d2i", //
			"d2l", //
			"d2f", //
			"i2b", //
			"i2c", //
			"i2s", //
			"lcmp", //
			"fcmpl", //
			"fcmpg", //
			"dcmpl", //
			"dcmpg", //
			"ifeq", //
			"ifne", //
			"iflt", //
			"ifge", //
			"ifgt", //
			"ifle", //
			"if_icmpeq", //
			"if_icmpne", //
			"if_icmplt", //
			"if_icmpge", //
			"if_icmpgt", //
			"if_icmple", //
			"if_acmpeq", //
			"if_acmpne", //
			"goto", //
			"jsr", //
			"ret", //
			"tableswitch", //
			"lookupswitch", //
			"ireturn", //
			"lreturn", //
			"freturn", //
			"dreturn", //
			"areturn", //
			"return", //
			"getstatic", //
			"putstatic", //
			"getfield", //
			"putfield", //
			"invokevirtual", //
			"invokespecial", //
			"invokestatic", //
			"invokeinterface", //
			"invokedynamic", //
			"new", //
			"newarray", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"anewarray", //
			"arraylength", //
			"athrow", //
			"checkcast", //
			"instanceof", //
			"monitorenter", //
			"monitorexit", //
			"wide", //
			"multianewarray", //
			"ifnull", //
			"ifnonnull", //
			"goto_w", //
			"jsr_w", //
			"breakpoint", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"undefined", //
			"impdep1", //
			"impdep2", //
	};
	
	public String toString() {
		// if (!name.equals("replace")) return "";
		StringWriter sb = new StringWriter();
		PrintWriter pw = new PrintWriter(sb);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(code));
		try {
			while (in.available() > 0) {
				int bc = in.read();
				switch (bc) {
				case 16:
					pw.println("bipush " + in.read());
					break;
				case 17:
					pw.println("sipush " + in.readShort());
					break;
				case 18:
					pw.println("ldc " + cf.resolveConstant(in.read()));
					break;
				case 19:
					pw.println("ldc_w " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 20:
					pw.println("ldc2_w " + cf.resolveConstant(in.readUnsignedShort()));
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
				case 132:
					pw.println("iinc " + in.read() + " " + in.read());
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
					pw.println("ret " + in.read());
					break;
				case 170:
					pw.println("tableswitch");
					break;
				case 171:
					pw.println("lookupswitch");
					// TODO: make
					break;
				case 178:
					pw.println("getstatic " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 179:
					pw.println("putstatic " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 180:
					pw.println("getfield " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 181:
					pw.println("putfield " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 182:
					pw.println("invokevirtual " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 183:
					pw.println("invokespecial " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 184:
					pw.println("invokestatic " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 185:
					pw.println("invokeinterface " + cf.resolveConstant(in.readUnsignedShort()) + " " + in.read());
					break;
				case 186:
					pw.println("invokedynamic " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 187:
					pw.println("new " + cf.resolveConstant(in.readUnsignedShort()));
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
					pw.println("anewarray " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 192:
					pw.println("checkcast " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 193:
					pw.println("instanceof " + cf.resolveConstant(in.readUnsignedShort()));
					break;
				case 197:
					pw.println("multianewarray " + cf.resolveConstant(in.readUnsignedShort()) + " " + in.read());
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
				default:
					pw.println(opcodes[bc]);
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
			pw.println("// IO ERROR!!!!");
		}
		return sb.toString();
	}
}
