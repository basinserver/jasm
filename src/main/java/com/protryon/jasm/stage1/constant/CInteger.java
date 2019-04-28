package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;

public class CInteger extends CConstant<Integer> {
	public CInteger(int i) {
		super(i, 3);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(this.value);
	}
}
