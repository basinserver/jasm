package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CInteger extends ConstantInfo {
	public int integer = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		this.integer = in.readInt();
		return this;
	}
	
	@Override
	public String getName() {
		return "Integer";
	}
}
