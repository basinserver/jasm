package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CClass extends ConstantInfo {
	public int name_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		name_index = in.readUnsignedShort();
		return this;
	}
	
}
