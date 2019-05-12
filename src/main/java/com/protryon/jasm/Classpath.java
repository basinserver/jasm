package com.protryon.jasm;

import com.protryon.jasm.stage1.Stage1Class;
import com.shapesecurity.functional.data.ImmutableSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Classpath {

    private Map<String, Resource> resources = new LinkedHashMap<>();
    private Map<String, Klass> klasses = new LinkedHashMap<>();
    private Map<String, Klass> dummyKlasses = new LinkedHashMap<>();
    private Map<String, Klass> libraryKlasses = new LinkedHashMap<>();
    private static Map<String, Klass> jreKlasses = new LinkedHashMap<>();

    private static Map<String, Stage1Class> initStage1(Map<String, Resource> resources) {
        Map<String, Stage1Class> stage1 = new LinkedHashMap<>();
        resources.forEach((key, value) -> {
            if (key.endsWith(".class")) {
                try {
                    String javaName = key;
                    if (javaName.contains(":")) {
                        javaName = javaName.substring(javaName.lastIndexOf(":") + 1);
                    }
                    if (javaName.startsWith("/")) {
                        javaName = javaName.substring(1);
                    }
                    javaName = javaName.substring(0, javaName.length() - ".class".length());
                    DataInputStream stream = new DataInputStream(new ByteArrayInputStream(value.content));
                    stage1.put(javaName, new Stage1Class(stream, value.isLibraryResource));
                    stream.close();
                } catch (IOException e) {
                    System.err.println("failed to read class: " + key);
                    e.printStackTrace();
                }
            }
        });
        return stage1;
    }

    static {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new RuntimeException("Please define JAVA_HOME");
        }
        Path jmods = Paths.get(javaHome, "jmods");
        if (!jmods.toFile().isDirectory()) {
            throw new RuntimeException("$JAVA_HOME/jmods directory does not exist");
        }
        Map<String, Resource> resources = new LinkedHashMap<>();
        for (File jmod : jmods.toFile().listFiles()) {
            if (!jmod.getAbsolutePath().endsWith(".jmod")) {
                continue;
            }
            try {
                ZipFile jar = new ZipFile(jmod);
                var entryIter = jar.entries().asIterator();
                for (ZipEntry entry; entryIter.hasNext();) {
                    entry = entryIter.next();
                    if (entry.getName().startsWith("classes/") && entry.getName().endsWith(".class")) {
                        String name = entry.getName().substring(8);
                        InputStream input = jar.getInputStream(entry);
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int i;
                        while ((i = input.read(buf, 0, 1024)) > 0) {
                            output.write(buf, 0, i);
                        }
                        resources.put(name, new Resource("jmod:" + jmod.getAbsolutePath() + ":" + name, output.toByteArray(), true));
                    }

                }
            } catch (Exception e) {
                throw new RuntimeException("failed to load jmod \"" + jmod.getAbsolutePath() + "\": ", e);
            }
        }
        var stage1 = initStage1(resources);
        stage1.forEach((javaName, stage1Class) -> {
            Klass klass = stage1Class.preClass();
            if (!klass.name.equals(javaName)) {
                throw new RuntimeException("Inconsistent java path vs package: \"" + klass.name + "\" vs \"" + javaName + "\".");
            }
            jreKlasses.put(klass.name, klass);
        });
        Classpath dummyClasspath;
        try {
            dummyClasspath = new Classpath(new String[0], new String[0]);
        } catch (IOException e) {
            throw new RuntimeException("not reached");
        }
        jreKlasses.forEach((name, klass) -> {
            stage1.get(name).midClass(dummyClasspath, klass);
        });

        System.out.println("Loaded " + jreKlasses.size() + " JRE classes");
    }

    public Classpath(String[] libraries, String[] targets) throws IOException {
        var librarySet = ImmutableSet.ofUsingEquality(libraries);
        var allPaths = new ArrayList<>(Arrays.asList(libraries));
        allPaths.addAll(Arrays.asList(targets));
        for (String path : allPaths) {
            boolean inLibrary = librarySet.contains(path);
            recurDir(Paths.get(path), Paths.get(path) ,0, inLibrary);
        }
        Map<String, Stage1Class> stage1 = initStage1(resources);
        stage1.forEach((javaName, stage1Class) -> {
            Klass klass = stage1Class.preClass();
            if (!klass.name.equals(javaName)) {
                throw new RuntimeException("Inconsistent java path vs package: \"" + klass.name + "\" vs \"" + javaName + "\".");
            }
            if (stage1Class.isLibrary) {
                libraryKlasses.put(klass.name, klass);
            } else {
                klasses.put(klass.name, klass);
            }
        });
        System.out.println("Loaded " + libraryKlasses.size() + " library classes");
        klasses.forEach((name, klass) -> {
            stage1.get(name).midClass(this, klass);
        });
        libraryKlasses.forEach((name, klass) -> {
            stage1.get(name).midClass(this, klass);
        });
        klasses.forEach((name, klass) -> {
            if (stage1.get(name).isLibrary) {
                return;
            }
            try {
                stage1.get(name).finishClass(this, klass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void recurDir(Path relativeTo, Path path, int depth, boolean inLibrary) throws IOException {
        if (depth > 32) {
            throw new RuntimeException("cannot recur more than 32 directories deep (do you have a symlink?)");
        }
        File f = path.normalize().toFile();
        if (f.isDirectory()) {
            for (File subFile : f.listFiles()) {
                recurDir(relativeTo, subFile.toPath(), depth + 1, inLibrary);
            }
        } else if (f.isFile()) {
            if (f.getAbsolutePath().endsWith(".jar")) {
                System.out.println("loading jar: " + f.getAbsolutePath());
                JarFile jar = new JarFile(f);
                jar.getResources().forEach((key, value) -> {
                    if (key.startsWith("META-INF/")) {
                        return;
                    }
                    resources.put(key, new Resource("jar:" + path + ":" + key, value, inLibrary));
                });
            } else {
                System.out.println("loading file: " + f.getAbsolutePath());
                String name = f.toPath().relativize(relativeTo).toString();
                resources.put(name, new Resource(name, Files.readAllBytes(f.toPath()), inLibrary));
            }
        }
    }

    public Klass loadKlass(String name) {
        Klass jreKlass = jreKlasses.get(name);
        if (jreKlass != null) {
            return jreKlass;
        }
        Klass klass = klasses.get(name);
        if (klass != null) {
            return klass;
        }
        klass = libraryKlasses.get(name);
        if (klass != null) {
            return klass;
        }
        System.err.println("[WARNING] failed to find class: " + name);
        // return null;
        // throw new RuntimeException("failed to find klass", new ClassNotFoundException(name));

        klass = dummyKlasses.get(name);
        if (klass != null) {
            return klass;
        }
        Klass dummy = new Klass(52, 0, name);
        dummyKlasses.put(name, dummy);
        return dummy;
    }

    public Map<String, Klass> getKlasses() {
        return klasses;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public Resource lookupResource(String name) {
        return resources.get(name);
    }


}
