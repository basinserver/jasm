package com.protryon.jasm.stage1.constant;

import com.protryon.jasm.stage1.CConstant;

import java.io.DataOutputStream;
import java.io.IOException;

public class CPackage extends CConstant<Integer> {
	public CPackage(int ref) {
		super(ref, 20);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value);
	}
	
}
