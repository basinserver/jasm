package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;

public class CMethodType extends CConstant<Integer> {
	public CMethodType(int descriptor_ref) {
		super(descriptor_ref, 16);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value);
	}
}
