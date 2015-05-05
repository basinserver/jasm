package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public abstract class ConstantInfo {
	protected final ClassFile cf;
	protected final int index;
	public final CType type;
	
	public ConstantInfo(CType type, ClassFile cf, int index) {
		this.cf = cf;
		this.index = index;
		this.type = type;
	}
	
	public abstract ConstantInfo read(DataInputStream in) throws IOException;
	
	public abstract ConstantInfo write(DataOutputStream out) throws IOException;
	
	public abstract ConstantInfo from(String s) throws Exception;
	
	public abstract String getName();
	
	public String toString() {
		return cf.resolveConstant(index, false);
	}
}
