package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CInteger extends ConstantInfo {
	public CInteger(ClassFile cf, int index) {
		super(CType.INTEGER, cf, index);
	}
	
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
	
	@Override
	public String toString() {
		return integer + "";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		integer = Integer.parseInt(s);
		return this;
	}
}
