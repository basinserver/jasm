package com.javaprophet.jasm.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;
import com.javaprophet.jasm.attribute.AttributeInfo;
import com.javaprophet.jasm.constant.CUTF8;

public class Code {
	private final ClassFile cf;
	private final String name;
	
	public Code(String name, ClassFile cf) {
		this.cf = cf;
		this.name = name;
	}
	
	public int max_stack = -1, max_locals = -1;
	public ExceptionEntry[] ees = null;
	public AttributeInfo[] ai = null;
	public InstructionSet code = null;
	public int name_index = -1;
	public StackMapTable smt = null;
	
	public Code read(int name_index, DataInputStream in) throws IOException {
		this.name_index = name_index;
		in.readInt(); // TODO: attribute length, but we dont need it?
		max_stack = in.readUnsignedShort();
		max_locals = in.readUnsignedShort();
		byte[] code = new byte[in.readInt()];
		in.readFully(code);
		this.code = new InstructionSet(cf).read(code);
		int eel = in.readUnsignedShort();
		ees = new ExceptionEntry[eel];
		for (int i = 0; i < ees.length; i++) {
			ees[i] = new ExceptionEntry().read(in);
		}
		int ac = in.readUnsignedShort();
		ai = new AttributeInfo[ac];
		for (int i = 0; i < ai.length; i++) {
			int ni2 = in.readUnsignedShort();
			String name = ((CUTF8)cf.getConstant(ni2)).utf;
			if (name.equals("StackMapTable")) {
				smt = new StackMapTable(this, cf).read(ni2, in);
			}else {
				ai[i] = new AttributeInfo().read(ni2, in);
			}
		}
		return this;
	}
	
	public Code write(DataOutputStream out) throws IOException {
		out.writeShort(name_index);
		ByteArrayOutputStream tout = new ByteArrayOutputStream();
		DataOutputStream tout2 = new DataOutputStream(tout);
		tout2.writeShort(max_stack);
		tout2.writeShort(max_locals);
		if (code != null) {
			byte[] c = code.write();
			tout2.writeInt(c.length);
			tout2.write(c);
		}
		tout2.writeShort(ees.length);
		for (int i = 0; i < ees.length; i++) {
			ees[i].write(tout2);
		}
		tout2.writeShort(ai.length);
		for (int i = 0; i < ai.length; i++) {
			ai[i].write(tout2);
		}
		out.writeInt(tout.size());
		out.write(tout.toByteArray());
		return this;
	}
}
