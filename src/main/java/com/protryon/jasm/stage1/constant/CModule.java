package com.protryon.jasm.stage1.constant;

import com.protryon.jasm.stage1.CConstant;

import java.io.DataOutputStream;
import java.io.IOException;

public class CModule extends CConstant<Integer> {
	public CModule(int ref) {
		super(ref, 19);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value);
	}
	
}
