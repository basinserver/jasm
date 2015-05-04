package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CInvokeDynamic extends ConstantInfo {
	public int bootstrap_method_attr_index = -1, name_and_type_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		bootstrap_method_attr_index = in.readUnsignedShort();
		name_and_type_index = in.readUnsignedShort();
		return this;
	}
	
}
