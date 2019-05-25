package com.protryon.jasm

import com.protryon.jasm.stage1.Stage1Class
import com.shapesecurity.functional.data.ImmutableSet

import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Arrays
import java.util.LinkedHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class Classpath @Throws(IOException::class)
constructor(libraries: Array<String>, targets: Array<String>) {

    val resources = LinkedHashMap<String, Resource>()
    private val klasses = LinkedHashMap<String, Klass>()
    private val dummyKlasses = LinkedHashMap<String, Klass>()
    private val libraryKlasses = LinkedHashMap<String, Klass>()

    init {
        val librarySet = ImmutableSet.ofUsingEquality(*libraries)
        val allPaths = ArrayList(Arrays.asList(*libraries))
        allPaths.addAll(Arrays.asList(*targets))
        for (path in allPaths) {
            val inLibrary = librarySet.contains(path)
            recurDir(Paths.get(path), Paths.get(path), 0, inLibrary)
        }
        val stage1 = initStage1(resources)
        stage1.forEach { javaName, stage1Class ->
            val klass = stage1Class.preClass()
            if (klass.name != javaName) {
                throw RuntimeException("Inconsistent java path vs package: \"" + klass.name + "\" vs \"" + javaName + "\".")
            }
            if (stage1Class.isLibrary) {
                libraryKlasses[klass.name] = klass
            } else {
                klasses[klass.name] = klass
            }
        }
        println("Loaded " + libraryKlasses.size + " library classes")
        klasses.forEach { name, klass -> stage1.getValue(name).midClass(this, klass) }
        libraryKlasses.forEach { name, klass -> stage1.getValue(name).midClass(this, klass) }
        klasses.forEach { name, klass ->
            if (!stage1.getValue(name).isLibrary) {
                try {
                    stage1.getValue(name).finishClass(this, klass)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun recurDir(relativeTo: Path, path: Path, depth: Int, inLibrary: Boolean) {
        if (depth > 32) {
            throw RuntimeException("cannot recur more than 32 directories deep (do you have a symlink?)")
        }
        val f = path.normalize().toFile()
        if (f.isDirectory) {
            for (subFile in f.listFiles()) {
                recurDir(relativeTo, subFile.toPath(), depth + 1, inLibrary)
            }
        } else if (f.isFile) {
            if (f.absolutePath.endsWith(".jar")) {
                println("loading jar: " + f.absolutePath)
                val jar = JarFile(f)
                jar.resources.forEach { key, value ->
                    if (!key.startsWith("META-INF/")) {
                        resources[key] = Resource("jar:$path:$key", value, inLibrary)
                    }
                }
            } else {
                println("loading file: " + f.absolutePath)
                val name = f.toPath().relativize(relativeTo).toString()
                resources[name] = Resource(name, Files.readAllBytes(f.toPath()), inLibrary)
            }
        }
    }

    fun loadKlass(name: String): Klass {
        val jreKlass = jreKlasses[name]
        if (jreKlass != null) {
            return jreKlass
        }
        var klass: Klass? = klasses[name]
        if (klass != null) {
            return klass
        }
        klass = libraryKlasses[name]
        if (klass != null) {
            return klass
        }
        System.err.println("[WARNING] failed to find class: $name")
        // return null;
        // throw new RuntimeException("failed to find klass", new ClassNotFoundException(name));

        klass = dummyKlasses[name]
        if (klass != null) {
            return klass
        }
        val dummy = Klass(52, 0, name)
        dummyKlasses[name] = dummy
        return dummy
    }

    fun getKlasses(): Map<String, Klass> {
        return klasses
    }

    companion object {
        private val jreKlasses = LinkedHashMap<String, Klass>()

        private fun initStage1(resources: Map<String, Resource>): Map<String, Stage1Class> {
            val stage1 = LinkedHashMap<String, Stage1Class>()
            resources.forEach { key, value ->
                if (key.endsWith(".class")) {
                    try {
                        var javaName = key
                        if (javaName.contains(":")) {
                            javaName = javaName.substring(javaName.lastIndexOf(":") + 1)
                        }
                        if (javaName.startsWith("/")) {
                            javaName = javaName.substring(1)
                        }
                        javaName = javaName.substring(0, javaName.length - ".class".length)
                        val stream = DataInputStream(ByteArrayInputStream(value.content))
                        stage1[javaName] = Stage1Class(stream, value.isLibraryResource)
                        stream.close()
                    } catch (e: IOException) {
                        System.err.println("failed to read class: $key")
                        e.printStackTrace()
                    }

                }
            }
            return stage1
        }

        init {
            val javaHome = System.getProperty("java.home") ?: throw RuntimeException("Please define JAVA_HOME")
            val jmods = Paths.get(javaHome, "jmods")
            if (!jmods.toFile().isDirectory) {
                throw RuntimeException("\$JAVA_HOME/jmods directory does not exist")
            }
            val resources = LinkedHashMap<String, Resource>()
            for (jmod in jmods.toFile().listFiles()) {
                if (!jmod.absolutePath.endsWith(".jmod")) {
                    continue
                }
                try {
                    val jar = ZipFile(jmod)
                    val entryIter = jar.entries().asIterator()
                    var entry: ZipEntry
                    while (entryIter.hasNext()) {
                        entry = entryIter.next()
                        if (entry.name.startsWith("classes/") && entry.name.endsWith(".class")) {
                            val name = entry.name.substring(8)
                            val input = jar.getInputStream(entry)
                            val output = ByteArrayOutputStream()
                            val buf = ByteArray(1024)
                            var i: Int
                            while (true) {
                                i = input.read(buf, 0, 1024)
                                if (i <= 0) {
                                    break
                                }
                                output.write(buf, 0, i)
                            }
                            resources[name] = Resource("jmod:" + jmod.absolutePath + ":" + name, output.toByteArray(), true)
                        }

                    }
                } catch (e: Exception) {
                    throw RuntimeException("failed to load jmod \"" + jmod.absolutePath + "\": ", e)
                }

            }
            val stage1 = initStage1(resources)
            stage1.forEach { javaName, stage1Class ->
                val klass = stage1Class.preClass()
                if (klass.name != javaName) {
                    throw RuntimeException("Inconsistent java path vs package: \"" + klass.name + "\" vs \"" + javaName + "\".")
                }
                jreKlasses[klass.name] = klass
            }
            val dummyClasspath: Classpath
            try {
                dummyClasspath = Classpath(Array(0) {""}, Array(0) {""})
            } catch (e: IOException) {
                throw RuntimeException("not reached")
            }

            jreKlasses.forEach { name, klass -> stage1.getValue(name).midClass(dummyClasspath, klass) }

            println("Loaded " + jreKlasses.size + " JRE classes")
        }
    }


}
