package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class ConstantInfo {
	public abstract ConstantInfo read(DataInputStream in) throws IOException;
}
