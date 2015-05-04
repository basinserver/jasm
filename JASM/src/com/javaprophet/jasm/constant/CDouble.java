package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CDouble extends ConstantInfo {
	public double dbl = -1D;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		dbl = in.readDouble();
		return this;
	}
	
	@Override
	public String getName() {
		return "Double";
	}
	
}
