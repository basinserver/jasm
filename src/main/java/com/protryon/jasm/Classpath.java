package com.protryon.jasm;

import com.protryon.jasm.stage1.Stage1Class;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class Classpath {

    private Map<String, Resource> resources = new LinkedHashMap<>();
    private Map<String, Klass> klasses = new LinkedHashMap<>();
    private Map<String, Klass> dummyKlasses = new LinkedHashMap<>();

    public Classpath(String... paths) throws IOException {
        for (String path : paths) {
            if (path.endsWith(".jar")) {
                JarFile jar = new JarFile(new File(path));
                jar.getResources().forEach((key, value) -> {
                    resources.put(key, new Resource("jar:" + path + ":" + key, value));
                });
            } else if (path.endsWith(".class")) {
                resources.put(path, new Resource(path, Files.readAllBytes(Paths.get(path))));
            } else {
                recurDir(Paths.get(path), Paths.get(path) ,0);
            }
        }
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
                    stage1.put(javaName, new Stage1Class(new DataInputStream(new ByteArrayInputStream(value.content)), true));
                } catch (IOException e) {
                    System.err.println("failed to read class: " + key);
                    e.printStackTrace();
                }
            }
        });
        stage1.forEach((javaName, stage1Class) -> {
            Klass klass = stage1Class.preClass();
            if (!klass.name.equals(javaName)) {
                throw new RuntimeException("Inconsistent java path vs package: \"" + klass.name + "\" vs \"" + javaName + "\".");
            }
            klasses.put(klass.name, klass);
        });
        klasses.forEach((name, klass) -> {
            stage1.get(name).midClass(this, klass);
        });
        klasses.forEach((name, klass) -> {
            try {
                stage1.get(name).finishClass(this, klass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void recurDir(Path relativeTo, Path path, int depth) throws IOException {
        if (depth > 32) {
            throw new RuntimeException("cannot recur more than 32 directories deep (do you have a symlink?)");
        }
        File f = path.normalize().toFile();
        if (f.isDirectory()) {
            for (File subFile : f.listFiles()) {
                recurDir(relativeTo, subFile.toPath(), depth + 1);
            }
        } else if (f.isFile()) {
            String name = f.toPath().relativize(relativeTo).toString();
            resources.put(name, new Resource(name, Files.readAllBytes(f.toPath())));
        }
    }

    public Klass loadKlass(String name) {
        Klass klass = klasses.get(name);
        if (klass != null) {
            return klass;
        }
        klass = dummyKlasses.get(name);
        if (klass != null) {
            return klass;
        }
        Klass dummy = new Klass(52, 0, name);
        dummyKlasses.put(name, dummy);
        return dummy;
    }

    public Klass lookupKlass(String name) {
        return klasses.get(name);
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
