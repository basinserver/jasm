package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CDouble extends ConstantInfo {
	public CDouble(ClassFile cf, int index) {
		super(CType.DOUBLE, cf, index);
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
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		dbl = Double.parseDouble(s);
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		out.writeDouble(dbl);
		return this;
	}
}
