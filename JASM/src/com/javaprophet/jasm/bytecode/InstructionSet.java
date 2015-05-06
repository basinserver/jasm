package com.javaprophet.jasm.bytecode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
	
	public static int opcodeLength(String op) {
		if (op.equals("bipush")) {
			return 2;
		}else if (op.equals("sipush")) {
			return 3;
		}else if (op.equals("ldc")) {
			return 2;
		}else if (op.equals("ldc_w")) {
			return 3;
		}else if (op.equals("ldc2_w")) {
			return 3;
		}else if (op.equals("iload")) {
			return 2;
		}else if (op.equals("lload")) {
			return 2;
		}else if (op.equals("fload")) {
			return 2;
		}else if (op.equals("dload")) {
			return 2;
		}else if (op.equals("aload")) {
			return 2;
		}else if (op.equals("istore")) {
			return 2;
		}else if (op.equals("lstore")) {
			return 2;
		}else if (op.equals("fstore")) {
			return 2;
		}else if (op.equals("dstore")) {
			return 2;
		}else if (op.equals("astore")) {
			return 2;
		}else if (op.equals("iinc")) {
			return 3;
		}else if (op.equals("ifeq")) {
			return 3;
		}else if (op.equals("ifne")) {
			return 3;
		}else if (op.equals("iflt")) {
			return 3;
		}else if (op.equals("ifge")) {
			return 3;
		}else if (op.equals("ifgt")) {
			return 3;
		}else if (op.equals("ifle")) {
			return 3;
		}else if (op.equals("if_icmpeq")) {
			return 3;
		}else if (op.equals("if_icmpne")) {
			return 3;
		}else if (op.equals("if_icmplt")) {
			return 3;
		}else if (op.equals("if_icmpge")) {
			return 3;
		}else if (op.equals("if_icmpgt")) {
			return 3;
		}else if (op.equals("if_icmple")) {
			return 3;
		}else if (op.equals("if_icmpeq")) {
			return 3;
		}else if (op.equals("if_acmpne")) {
			return 3;
		}else if (op.equals("goto")) {
			return 3;
		}else if (op.equals("jsr")) {
			return 3;
		}else if (op.equals("ret")) {
			return 2;
		}else if (op.equals("tableswitch")) {// TODO
		}else if (op.equals("lookupswitch")) {// TODO
		}else if (op.equals("getstatic")) {
			return 3;
		}else if (op.equals("putstatic")) {
			return 3;
		}else if (op.equals("getfield")) {
			return 3;
		}else if (op.equals("putfield")) {
			return 3;
		}else if (op.equals("invokevirtual")) {
			return 3;
		}else if (op.equals("invokespecial")) {
			return 3;
		}else if (op.equals("invokestatic")) {
			return 3;
		}else if (op.equals("invokeinterface")) {
			return 3;
		}else if (op.equals("invokedynamic")) {
			return 3;
		}else if (op.equals("new")) {
			return 3;
		}else if (op.equals("newarray")) {
			return 2;
		}else if (op.equals("anewarray")) {
			return 3;
		}else if (op.equals("checkcast")) {
			return 3;
		}else if (op.equals("instanceof")) {
			return 3;
		}else if (op.equals("multianewarray")) {
			return 4;
		}else if (op.equals("ifnull")) {
			return 3;
		}else if (op.equals("ifnonnull")) {
			return 3;
		}else if (op.equals("goto_w")) {
			return 5;
		}else if (op.equals("jsr_w")) {
			return 5;
		}else {
			return 1;
		}
		return 0;
	}
	
	public byte[] write() {
		return this.code;
	}
	
	public void fromString(String s) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		Scanner in = new Scanner(s);
		int ri = 0;
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line.contains("//")) line = line.substring(0, line.indexOf("//"));
			line = line.trim();
			if (line.length() == 0) continue;
			String[] args = line.split(" ");
			if (args.length == 0) continue;
			for (int i = 0; i < opcodes.length; i++) {
				if (opcodes[i].equals(args[0])) {
					out.write(i);
				}else continue;
				// TODO: syntax checking
				if (args[0].equals("bipush")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("sipush")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ldc")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("ldc_w")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ldc2_w")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("iload")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("lload")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("fload")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("dload")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("aload")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("istore")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("lstore")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("fstore")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("dstore")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("astore")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("iinc")) {
					out.write(Integer.parseInt(args[1]));
					out.write(Integer.parseInt(args[2]));
				}else if (args[0].equals("ifeq")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ifne")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("iflt")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ifge")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ifgt")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ifle")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_icmpeq")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_icmpne")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_icmplt")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_icmpge")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_icmpgt")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_icmple")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_icmpeq")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("if_acmpne")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("goto")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("jsr")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ret")) {
					out.write(Integer.parseInt(args[1]));
				}else if (args[0].equals("tableswitch")) {
				}else if (args[0].equals("lookupswitch")) {
				}else if (args[0].equals("getstatic")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("putstatic")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("getfield")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("putfield")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("invokevirtual")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("invokespecial")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("invokestatic")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("invokeinterface")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("invokedynamic")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("new")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("newarray")) {
					int r = 0;
					if (args[1].equalsIgnoreCase("T_BOOLEAN")) {
						r = 4;
					}else if (args[1].equalsIgnoreCase("T_CHAR")) {
						r = 5;
					}else if (args[1].equalsIgnoreCase("T_FLOAT")) {
						r = 6;
					}else if (args[1].equalsIgnoreCase("T_DOUBLE")) {
						r = 7;
					}else if (args[1].equalsIgnoreCase("T_BYTE")) {
						r = 8;
					}else if (args[1].equalsIgnoreCase("T_SHORT")) {
						r = 9;
					}else if (args[1].equalsIgnoreCase("T_INT")) {
						r = 10;
					}else if (args[1].equalsIgnoreCase("T_LONG")) {
						r = 11;
					}else {
						// TODO: invalid arg
					}
					out.write(r);
				}else if (args[0].equals("anewarray")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("checkcast")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("instanceof")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("multianewarray")) {
					out.writeShort(Integer.parseInt(args[1]));
					out.write(Integer.parseInt(args[2]));
				}else if (args[0].equals("ifnull")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("ifnonnull")) {
					out.writeShort(Integer.parseInt(args[1]));
				}else if (args[0].equals("goto_w")) {
					out.writeInt(Integer.parseInt(args[1]));
				}else if (args[0].equals("jsr_w")) {
					out.writeInt(Integer.parseInt(args[1]));
				}
			}
		}
		this.code = bout.toByteArray();
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
		CountInputStream cin = new CountInputStream(new ByteArrayInputStream(code));
		DataInputStream in = new DataInputStream(cin);
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
					int ptr = in.readShort();
					pw.println("ifeq " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 154:
					ptr = in.readShort();
					pw.println("ifne " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 155:
					ptr = in.readShort();
					pw.println("iflt " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 156:
					ptr = in.readShort();
					pw.println("ifge " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 157:
					ptr = in.readShort();
					pw.println("ifgt " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 158:
					ptr = in.readShort();
					pw.println("ifle " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 159:
					ptr = in.readShort();
					pw.println("if_icmpeq " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 160:
					ptr = in.readShort();
					pw.println("if_icmpne " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 161:
					ptr = in.readShort();
					pw.println("if_icmplt " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 162:
					ptr = in.readShort();
					pw.println("if_icmpge " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 163:
					ptr = in.readShort();
					pw.println("if_icmpgt " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 164:
					ptr = in.readShort();
					pw.println("if_icmple " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 165:
					ptr = in.readShort();
					pw.println("if_acmpeq " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
					break;
				case 166:
					ptr = in.readShort();
					pw.println("if_acmpne " + ptr + " // points to " + (cin.getCount() - 3 + ptr));
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
