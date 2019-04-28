package com.protryon.jasm.stage1.constant;

import com.protryon.jasm.stage1.CConstant;

import java.io.DataOutputStream;
import java.io.IOException;

public class CString extends CConstant<Integer> {
	public CString(int ref) {
		super(ref, 8);
	}

	protected void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value);
	}
}
