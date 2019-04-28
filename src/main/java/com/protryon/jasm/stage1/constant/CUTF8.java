package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;

public class CUTF8 extends CConstant<String> {
	public CUTF8(String value) {
		super(value, 1);
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		byte[] utf = this.value.getBytes();
		out.writeShort(utf.length);
		out.write(utf);
	}

	public static CUTF8 assertCUTF8(CConstant CConstant) {
		if (!(CConstant instanceof CUTF8)) {
			throw new RuntimeException("Expected UTF8!");
		}
		return (CUTF8) CConstant;
	}
}
