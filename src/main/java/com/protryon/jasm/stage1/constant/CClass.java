package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;

public class CClass extends CConstant<Integer> {
	public CClass(int ref) {
		super(ref, 7);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value);
	}

	public static CClass assertCClass(CConstant CConstant) {
		if (!(CConstant instanceof CClass)) {
			throw new RuntimeException("Expected CClass!");
		}
		return (CClass) CConstant;
	}
	
}
