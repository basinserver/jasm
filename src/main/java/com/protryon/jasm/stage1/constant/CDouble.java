package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;

public class CDouble extends CConstant<Double> {
	public CDouble(double d) {
		super(d, 6);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeDouble(this.value);
	}

	public boolean isDoubled() {
		return true;
	}
}
