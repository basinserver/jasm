package com.javaprophet.jasm;

import java.io.File;
import java.io.IOException;
import com.javaprophet.jasm.bytecode.Code;
import com.javaprophet.jasm.constant.CUTF8;
import com.javaprophet.jasm.field.FieldInfo;
import com.javaprophet.jasm.method.MethodInfo;

public class Test {
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	public static void main(String[] args) {
		try {
			ClassFile cf = new ClassFile(new File("/windsk/stub/a.class"));
			System.out.println("Version: " + cf.getVersion());
			System.out.println("Class: " + cf.getClassName());
			System.out.println("Super Class: " + cf.getSuperClassName());
			FieldInfo[] fs = cf.getFieldPool();
			for (int i = 0; i < fs.length; i++) {
				FieldInfo f = fs[i];
				String name = ((CUTF8)cf.getConstant(f.name_index)).utf;
				System.out.println("field: " + name);
			}
			MethodInfo[] ms = cf.getMethodPool();
			for (int i = 0; i < ms.length; i++) {
				MethodInfo m = ms[i];
				String name = ((CUTF8)cf.getConstant(m.name_index)).utf;
				System.out.println("method: " + name);
				Code code = m.code;
				System.out.println("code: " + code.code);
			}
			System.out.println();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
