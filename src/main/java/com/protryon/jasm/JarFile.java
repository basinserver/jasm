package com.protryon.jasm;

import java.io.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarFile {

    public JarFile(File f) throws IOException {
        this(f.toURI().toURL());
    }

    private final URL url;

    public URL getSource() {
        return url;
    }

    private Map<String, byte[]> resources = new LinkedHashMap<>();

    public Map<String, byte[]> getResources() {
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

//    public ReflectionClassLoader getReflector() {
//        ReflectionClassLoader rcl = new ReflectionClassLoader();
//        rcl.setPreloadCache(false);
//        rcl.loadJarFile(this);
//        rcl.flushToMemory();
//        return rcl;
//    }

    public String toString() {
        return url.toString();
    }

    public void read(InputStream in) throws IOException {
        ZipInputStream jarIn = new ZipInputStream(in);// not jar, so we can read manifest directly
        ZipEntry entry;
        this.resources.clear();
        while ((entry = jarIn.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int i = 1;
            while (i > 0) {
                i = jarIn.read(buf, 0, i);
                if (i > 0) {
                    out.write(buf, 0, i);
                }
            }
            this.resources.put(entry.getName(), out.toByteArray());
        }
        jarIn.close();
    }

    public void write(OutputStream out) throws IOException {
        JarOutputStream jarOut = new JarOutputStream(out);
        for (String s : resources.keySet()) {
            jarOut.putNextEntry(new ZipEntry(s));
            jarOut.write(resources.get(s));
            jarOut.closeEntry();
        }
        jarOut.close();
    }
}
