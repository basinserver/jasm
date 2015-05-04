package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CDouble extends ConstantInfo {
	public CDouble(ClassFile cf, int index) {
		super(cf, index);
	}
	
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
	
	@Override
	public String toString() {
		return dbl + "";
	}
	
}
