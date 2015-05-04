package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public abstract class ConstantInfo {
	protected final ClassFile cf;
	protected final int index;
	
	public ConstantInfo(ClassFile cf, int index) {
		this.cf = cf;
		this.index = index;
	}
	
	public abstract ConstantInfo read(DataInputStream in) throws IOException;
	
	public abstract String getName();
	
	public String toString() {
		return cf.resolveConstant(index, false);
	}
}
