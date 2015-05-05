package com.javaprophet.jasm.bytecode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExceptionEntry {
	public int start_pc = -1, end_pc = -1, handler_pc = -1, catch_type = -1;
	
	public ExceptionEntry read(DataInputStream in) throws IOException {
		start_pc = in.readUnsignedShort();
		end_pc = in.readUnsignedShort();
		handler_pc = in.readUnsignedShort();
		catch_type = in.readUnsignedShort();
		return this;
	}
	
	public ExceptionEntry write(DataOutputStream out) throws IOException {
		out.writeShort(start_pc);
		out.writeShort(end_pc);
		out.writeShort(handler_pc);
		out.writeShort(catch_type);
		return this;
	}
}
