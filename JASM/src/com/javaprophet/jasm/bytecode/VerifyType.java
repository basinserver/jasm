package com.javaprophet.jasm.bytecode;

public class VerifyType {
	public static final VerifyType TOP = new VerifyType(0);
	public static final VerifyType INTEGER = new VerifyType(1);
	public static final VerifyType FLOAT = new VerifyType(2);
	public static final VerifyType DOUBLE = new VerifyType(3);
	public static final VerifyType LONG = new VerifyType(4);
	public static final VerifyType NULL = new VerifyType(5);
	public static final VerifyType UNINITIALZEDTHIS = new VerifyType(6);
	public final int i;
	
	public VerifyType(int i) {
		this.i = i;
	}
}
