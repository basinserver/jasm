package com.javaprophet.jasm;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.javaprophet.jasm.reflection.ReflectionClassLoader;

public class JarFile {
	public JarFile(File f) throws IOException {
		this(f.toURI().toURL());
	}
	
	@Deprecated
	public JarFile() {
		this.url = null;
	}
	
	private final URL url;
	
	public URL getSource() {
		return url;
	}
	
	private ClassFile[] cfs = null;
	private ConcurrentHashMap<String, byte[]> resources = new ConcurrentHashMap<String, byte[]>();
	
	public ClassFile[] getClasses() {
		return cfs;
	}
	
	public ConcurrentHashMap<String, byte[]> getResources() {
		return resources;
	}
	
	public JarFile(URL url) throws IOException {
		this.url = url;
		read(this.url.openStream());
	}
	
	public String getMainClass() {
		if (!resources.containsKey("META-INF/MANIFEST.MF")) return null;
		Scanner in = new Scanner(new String(resources.get("META-INF/MANIFEST.MF")));
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();
			if (line.startsWith("Main-Class:")) {
				in.close();
				return line.substring(11).trim();
			}
		}
		in.close();
		return null;
	}
	
	public ReflectionClassLoader getReflector() {
		ReflectionClassLoader rcl = new ReflectionClassLoader();
		rcl.setPreloadCache(false);
		rcl.loadJarFile(this);
		rcl.flushToMemory();
		return rcl;
	}
	
	@Deprecated
	public JarFile(InputStream in) throws IOException { // TODO: tap into reflectionclassloader's custom url system
		this.url = null; // classloading/reflection wont work, but is fine for just editing bytecode
		read(in);
	}
	
	public String toString() {
		String mc = getMainClass();
		if (mc != null) {
			mc = mc.replace(".", "/");
			for (ClassFile cf : cfs) {
				if (cf.getClassName().equals(mc)) {
					return cf.getClassName();
				}
			}
		}else if (url != null) {
			return url.toString();
		}
		return "Jar File";
	}
	
	public void read(InputStream in) throws IOException {
		ZipInputStream jin = new ZipInputStream(in);// not jar, so we can read manifest directly
		ZipEntry entry;
		this.resources.clear();
		ArrayList<ClassFile> tcfs = new ArrayList<ClassFile>();
		while ((entry = jin.getNextEntry()) != null) {
			if (entry.getName().endsWith(".class")) {
				tcfs.add(new ClassFile(jin));
			}else {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int i = 1;
				while (i > 0) {
					i = jin.read(buf, 0, i);
					if (i > 0) {
						bout.write(buf, 0, i);
					}
				}
				this.resources.put(entry.getName(), bout.toByteArray()); // TODO: classes as resources
			}
		}
		this.cfs = tcfs.toArray(new ClassFile[]{});
		jin.close();
	}
	
	public void write(OutputStream out) throws IOException {
		JarOutputStream jout = new JarOutputStream(out);
		for (String s : resources.keySet()) {
			jout.putNextEntry(new ZipEntry(s));
			jout.write(resources.get(s));
			jout.closeEntry();
		}
		for (ClassFile cf : cfs) {
			String name = cf.getClassName() + ".class";
			jout.putNextEntry(new ZipEntry(name));
			DataOutputStream dout = new DataOutputStream(jout);
			cf.write(dout);
			jout.closeEntry();
		}
		jout.close();
	}
}
