package com.javaprophet.jasm.bytecode;

public class VerifyTypeObject extends VerifyType {
	public int cpool_index = -1;
	
	public VerifyTypeObject(int cpool_index) {
		super(7);
		this.cpool_index = cpool_index;
	}
}
