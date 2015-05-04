package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CMethodType extends ConstantInfo {
	public CMethodType(ClassFile cf, int index) {
		super(cf, index);
	}
	
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
