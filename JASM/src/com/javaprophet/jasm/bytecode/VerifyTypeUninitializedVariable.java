package com.javaprophet.jasm.bytecode;

public class VerifyTypeUninitializedVariable extends VerifyType {
	public int offset = -1;
	
	public VerifyTypeUninitializedVariable(int offset) {
		super(8);
		this.offset = offset;
	}
}
