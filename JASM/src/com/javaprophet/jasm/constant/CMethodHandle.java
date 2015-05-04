package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CMethodHandle extends ConstantInfo {
	public int reference_type = -1, reference_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		reference_type = in.read();
		reference_index = in.readUnsignedShort();
		return this;
	}
	
}
