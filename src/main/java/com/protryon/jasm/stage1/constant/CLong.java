package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;

public class CLong extends CConstant<Long> {
	public CLong(long lng) {
		super(lng, 5);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeLong(this.value);
	}

	public boolean isDoubled() {
		return true;
	}
}
