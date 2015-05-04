package com.javaprophet.jasm;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.javaprophet.jasm.attribute.AttributeInfo;
import com.javaprophet.jasm.constant.CClass;
import com.javaprophet.jasm.constant.CDouble;
import com.javaprophet.jasm.constant.CFieldRef;
import com.javaprophet.jasm.constant.CFloat;
import com.javaprophet.jasm.constant.CInteger;
import com.javaprophet.jasm.constant.CInterfaceMethodRef;
import com.javaprophet.jasm.constant.CInvokeDynamic;
import com.javaprophet.jasm.constant.CLong;
import com.javaprophet.jasm.constant.CMethodHandle;
import com.javaprophet.jasm.constant.CMethodRef;
import com.javaprophet.jasm.constant.CMethodType;
import com.javaprophet.jasm.constant.CNameAndType;
import com.javaprophet.jasm.constant.CString;
import com.javaprophet.jasm.constant.CUTF8;
import com.javaprophet.jasm.constant.ConstantInfo;
import com.javaprophet.jasm.field.FieldInfo;
import com.javaprophet.jasm.method.MethodInfo;

public class ClassFile {
	public ClassFile(File f) throws IOException {
		this(new FileInputStream(f)); // TODO: ensure closed
	}
	
	public ClassFile(byte[] ba) throws IOException {
		this(new ByteArrayInputStream(ba));
	}
	
	private final DataInputStream in;
	
	public ClassFile(InputStream in) throws IOException {
		this.in = new DataInputStream(in);
		read();
	}
	
	public String getVersion() {
		return version;
	}
	
	public ConstantInfo[] getConstantPool() {
		return ci;
	}
	
	public FieldInfo[] getFieldPool() {
		return fi;
	}
	
	public FieldInfo getField(int i) {
		return fi[i];
	}
	
	public MethodInfo[] getMethodPool() {
		return mi;
	}
	
	public MethodInfo getMethod(int i) {
		return mi[i];
	}
	
	public AttributeInfo[] getAttributePool() {
		return ai;
	}
	
	public AttributeInfo getAttribute(int i) {
		return ai[i];
	}
	
	public ConstantInfo getConstant(int i) {
		return ci[i];
	}
	
	public int getClassIndex() {
		return thisClass;
	}
	
	public int getSuperClassIndex() {
		return superClass;
	}
	
	public int getAccessFlags() {
		return accessFlags;
	}
	
	public String toString() {
		return getClassName();
	}
	
	public String getClassName() {
		CClass pp = (CClass)ci[thisClass];
		CUTF8 name = (CUTF8)ci[pp.name_index];
		return name.utf;
	}
	
	public String getSuperClassName() {
		CClass pp = (CClass)ci[superClass];
		CUTF8 name = (CUTF8)ci[pp.name_index];
		return name.utf;
	}
	
	private String version = null;
	private ConstantInfo[] ci = null;
	private FieldInfo[] fi = null;
	private MethodInfo[] mi = null;
	private AttributeInfo[] ai = null;
	private int[] is = null;
	private int thisClass = -1, superClass = -1, accessFlags = -1;
	
	public void read() throws IOException {
		if (in.read() != 0x000000CA || in.read() != 0x000000FE || in.read() != 0x000000BA || in.read() != 0x000000BE) {
			throw new IOException("Not a Class File! Magic is not 0xCAFEBABE.");
		}
		int minor = in.readUnsignedShort();
		int major = in.readUnsignedShort();
		this.version = major + "." + minor;
		int cpc = in.readUnsignedShort();
		this.ci = new ConstantInfo[cpc];
		for (int i = 1; i < this.ci.length; i++) {
			int type = in.read();
			switch (type) {
			case 1:
				this.ci[i] = new CUTF8().read(in);
				break;
			case 3:
				this.ci[i] = new CInteger().read(in);
				break;
			case 4:
				this.ci[i] = new CFloat().read(in);
				break;
			case 5:
				this.ci[i] = new CLong().read(in);
				break;
			case 6:
				this.ci[i] = new CDouble().read(in);
				break;
			case 7:
				this.ci[i] = new CClass().read(in);
				break;
			case 8:
				this.ci[i] = new CString().read(in);
				break;
			case 9:
				this.ci[i] = new CFieldRef().read(in);
				break;
			case 10:
				this.ci[i] = new CMethodRef().read(in);
				break;
			case 11:
				this.ci[i] = new CInterfaceMethodRef().read(in);
				break;
			case 12:
				this.ci[i] = new CNameAndType().read(in);
				break;
			case 15:
				this.ci[i] = new CMethodHandle().read(in);
				break;
			case 16:
				this.ci[i] = new CMethodType().read(in);
				break;
			case 18:
				this.ci[i] = new CInvokeDynamic().read(in);
				break;
			}
		}
		accessFlags = in.readUnsignedShort();
		thisClass = in.readUnsignedShort();
		superClass = in.readUnsignedShort();
		int ifc = in.readUnsignedShort();
		this.is = new int[ifc];
		for (int i = 0; i < ifc; i++) {
			this.is[i] = in.readUnsignedShort(); // TODO: ?
		}
		int fc = in.readUnsignedShort();
		this.fi = new FieldInfo[fc];
		for (int i = 0; i < this.fi.length; i++) {
			this.fi[i] = new FieldInfo().read(in);
		}
		int mc = in.readUnsignedShort();
		this.mi = new MethodInfo[mc];
		for (int i = 0; i < mc; i++) {
			this.mi[i] = new MethodInfo(this).read(in);
		}
		int ac = in.readUnsignedShort();
		this.ai = new AttributeInfo[ac];
		for (int i = 0; i < ac; i++) {
			this.ai[i] = new AttributeInfo().read(in);
		}
	}
	
	public void close() throws IOException {
		in.close();
	}
}
