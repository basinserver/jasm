package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;

public class CFloat extends CConstant<Float> {
	public CFloat(float f) {
		super(f, 4);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeFloat(this.value);
	}
}
