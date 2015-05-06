package com.javaprophet.jasm.bytecode;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountInputStream extends FilterInputStream {
	
	protected CountInputStream(InputStream in) {
		super(in);
	}
	
	private int count = 0;
	
	public void resetCount() {
		count = 0;
	}
	
	public int read() throws IOException {
		count++;
		return super.read();
	}
	
	public int read(byte[] b) throws IOException {
		int c = super.read(b);
		count += c;
		return c;
	}
	
	public int read(byte[] b, int offset, int length) throws IOException {
		int c = super.read(b, offset, length);
		count += c;
		return c;
	}
	
	public long skip(long i) throws IOException {
		long c = super.skip(i);
		count += c;
		return c;
	}
	
	public int getCount() {
		return count;
	}
	
}
