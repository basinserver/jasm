package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CMethodRef extends ConstantInfo {
	public int class_index = -1, name_and_type_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		class_index = in.readUnsignedShort();
		name_and_type_index = in.readUnsignedShort();
		return this;
	}
}
