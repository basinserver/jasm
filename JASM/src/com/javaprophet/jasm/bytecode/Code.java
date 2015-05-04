package com.javaprophet.jasm.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;
import com.javaprophet.jasm.attribute.AttributeInfo;

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
	
	public Code read(DataInputStream in) throws IOException {
		in.readInt(); // TODO: attribute length, but we dont need it?
		max_stack = in.readUnsignedShort();
		max_locals = in.readUnsignedShort();
		byte[] code = new byte[in.readInt()];
		in.readFully(code);
		this.code = new InstructionSet(name, cf).read(code);
		int eel = in.readUnsignedShort();
		ees = new ExceptionEntry[eel];
		for (int i = 0; i < ees.length; i++) {
			ees[i] = new ExceptionEntry().read(in);
		}
		int ac = in.readUnsignedShort();
		ai = new AttributeInfo[ac];
		for (int i = 0; i < ai.length; i++) {
			ai[i] = new AttributeInfo().read(in);
		}
		return this;
	}
	
}
