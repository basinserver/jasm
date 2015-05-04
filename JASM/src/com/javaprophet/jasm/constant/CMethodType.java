package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CMethodType extends ConstantInfo {
	public int descriptor_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		descriptor_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "Method Type";
	}
}
