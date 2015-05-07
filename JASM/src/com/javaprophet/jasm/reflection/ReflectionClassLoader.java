package com.javaprophet.jasm.reflection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import com.javaprophet.jasm.ClassFile;
import com.javaprophet.jasm.JarFile;

public class ReflectionClassLoader extends ClassLoader {
	
	private boolean preloadCache = false;
	
	public ReflectionClassLoader() {
		super(ReflectionClassLoader.class.getClassLoader());
	}
	
	public void setPreloadCache(boolean preloadCache) {
		this.preloadCache = preloadCache;
	}
	
	public boolean getPreloadCache() {
		return this.preloadCache;
	}
	
	private final ConcurrentHashMap<String, Class<?>> loaded = new ConcurrentHashMap<String, Class<?>>();
	private final ConcurrentHashMap<String, ProtectionDomain> prots = new ConcurrentHashMap<String, ProtectionDomain>();
	private final ConcurrentHashMap<String, byte[]> preload = new ConcurrentHashMap<String, byte[]>();
	private final ConcurrentHashMap<String, byte[]> resources = new ConcurrentHashMap<String, byte[]>();
	
	private void preLoadClass(URL url, String name, byte[] cls) throws IOException {
		preload.put(name, cls);
		prots.put(name.substring(0, name.indexOf(".class")).replace("/", "."), new ProtectionDomain(new CodeSource(url, new Certificate[]{}), new Permissions()));
		// Class<?> clst = super.defineClass(name, cls, 0, cls.length);
		// loaded.put(clst.getName(), clst);
		// return clst;
	}
	
	private void finalizeLoading() {
		for (Object name : preload.keySet()) {
			if (!preload.containsKey((String)name)) continue;
			try {
				byte[] cls = preload.get((String)name);
				if (preloadCache) {
					Class<?> clsr = defineClass(((String)name).substring(0, ((String)name).indexOf(".class")).replace("/", "."), cls, 0, cls.length, prots.get(((String)name)));
					loaded.put(((String)name).substring(0, ((String)name).indexOf(".class")).replace("/", "."), clsr);
					preload.remove((String)name);
					prots.remove((String)name);
				}
			}catch (ClassFormatError e) {
				e.printStackTrace();
			}
		}
	}
	
	// public Class<?> loadClass(byte[] cls) throws IOException {
	// Class<?> clst = super.defineClass(cls, 0, cls.length);
	// loaded.put(clst.getName(), clst);
	// return clst;
	// }
	
	private static ClassLoader getAppClassLoader() {
		return sun.misc.Launcher.getLauncher().getClassLoader();
	}
	
	private final ConcurrentHashMap<String, Integer> cloaded = new ConcurrentHashMap<String, Integer>();
	
	public URL getResource(String name) {
		if (!resources.containsKey(name)) {
			return null;
		}
		// System.out.println(name + " resource requested!");
		try {
			if (cloaded.containsKey(name)) {
				return new URL("jasmrsrc:" + cloaded.get(name));
			}
			try {
				ClassLoader sun = getAppClassLoader();
				Class<?> aclb = sun.loadClass("com.javaprophet.jasm.reflection.AppClassLoaderBridge");
				int nid = aclb.getField("nextID").getInt(null);
				ConcurrentHashMap<Integer, byte[]> loaded = (ConcurrentHashMap<Integer, byte[]>)aclb.getField("sunrsrc").get(null);
				loaded.put(nid, resources.get(name));
				aclb.getField("nextID").setInt(null, nid + 1);
				cloaded.put(name, nid);
				return new URL("jasmrsrc:" + nid);
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public InputStream getResourceAsStream(String name) {
		if (resources.containsKey(name)) return new ByteArrayInputStream(resources.get(name));
		return null;
	}
	
	public void loadClassFile(URL source, ClassFile cf) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		try {
			cf.write(out);
			preLoadClass(source, cf.getClassName(), bout.toByteArray());
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadJarFile(JarFile jf) {
		this.resources.putAll(jf.getResources());
		for (ClassFile cf : jf.getClasses()) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bout);
			try {
				cf.write(out);
				preLoadClass(jf.getSource(), cf.getClassName(), bout.toByteArray());
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void loadJar(File home, InputStream in) throws IOException {
		JarInputStream jin = new JarInputStream(in);
		JarEntry entry;
		while ((entry = jin.getNextJarEntry()) != null) {
			if (entry.getName().endsWith(".class")) {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int i = 1;
				while (i > 0) {
					i = jin.read(buf);
					if (i > 0) {
						bout.write(buf, 0, i);
					}
				}
				preLoadClass(home.toURI().toURL(), entry.getName(), bout.toByteArray());
			}else {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int i = 1;
				while (i > 0) {
					i = jin.read(buf);
					if (i > 0) {
						bout.write(buf, 0, i);
					}
				}
				resources.put(entry.getName(), bout.toByteArray());
			}
		}
		jin.close();
	}
	
	public void loadJar(File f) throws IOException {
		if (!f.exists() || !f.canRead()) {
			throw new IOException("Can't read file!");
		}
		String fn = f.getName();
		if (fn.endsWith(".jar")) {
			FileInputStream fin = new FileInputStream(f);
			loadJar(f, fin);
			fin.close();
		}else {
			throw new IOException("You must specify a JAR file!");
		}
	}
	
	public void flushToMemory() {
		finalizeLoading();
	}
	
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (loaded.containsKey(name)) {
			return loaded.get(name);
		}
		if (preload.containsKey(name)) {
			byte[] clsb = preload.get(name);
			Class<?> cls = defineClass(name.substring(0, name.indexOf(".class")).replace("/", "."), clsb, 0, clsb.length, prots.get(name));
			loaded.put(name.substring(0, name.indexOf(".class")).replace("/", "."), cls);
			preload.remove(name);
			prots.remove(name);
			return cls;
		}
		return super.loadClass(name, resolve);
	}
	
	public Class<?> findClass(String name) throws ClassNotFoundException {
		if (loaded.containsKey(name)) {
			return loaded.get(name);
		}
		String pname = name.replace(".", "/") + ".class";
		if (preload.containsKey(pname)) {
			byte[] clsb = preload.get(pname);
			Class<?> cls = defineClass(pname.substring(0, pname.indexOf(".class")).replace("/", "."), clsb, 0, clsb.length, prots.get(name));
			loaded.put(pname.substring(0, pname.indexOf(".class")).replace("/", "."), cls);
			preload.remove(pname);
			prots.remove(name);
			return cls;
		}
		return super.findClass(name);
	}
}
