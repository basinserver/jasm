package com.protryon.jasm

import java.io.*
import java.net.URL
import java.util.LinkedHashMap
import java.util.Scanner
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class JarFile @Throws(IOException::class)
constructor(val source: URL) {

    val resources = LinkedHashMap<String, ByteArray>()

    val mainClass: String?
        get() {
            if (!resources.containsKey("META-INF/MANIFEST.MF")) return null
            val input = Scanner(String(resources["META-INF/MANIFEST.MF"]!!))
            while (input.hasNextLine()) {
                val line = input.nextLine().trim { it <= ' ' }
                if (line.startsWith("Main-Class:")) {
                    input.close()
                    return line.substring(11).trim { it <= ' ' }
                }
            }
            input.close()
            return null
        }

    @Throws(IOException::class)
    constructor(f: File) : this(f.toURI().toURL())

    init {
        read(this.source.openStream())
    }

    //    public ReflectionClassLoader getReflector() {
    //        ReflectionClassLoader rcl = new ReflectionClassLoader();
    //        rcl.setPreloadCache(false);
    //        rcl.loadJarFile(this);
    //        rcl.flushToMemory();
    //        return rcl;
    //    }

    override fun toString(): String {
        return source.toString()
    }

    @Throws(IOException::class)
    fun read(`in`: InputStream) {
        val jarIn = ZipInputStream(`in`)// not jar, so we can read manifest directly
        var entry: ZipEntry?
        this.resources.clear()
        while (true) {
            entry = jarIn.nextEntry
            if (entry == null) {
                break
            }
            val out = ByteArrayOutputStream()
            val buf = ByteArray(1024)
            var i = 1
            while (i > 0) {
                i = jarIn.read(buf, 0, i)
                if (i > 0) {
                    out.write(buf, 0, i)
                }
            }
            this.resources[entry.name] = out.toByteArray()
        }
        jarIn.close()
    }

    @Throws(IOException::class)
    fun write(out: OutputStream) {
        val jarOut = JarOutputStream(out)
        for (s in resources.keys) {
            jarOut.putNextEntry(ZipEntry(s))
            jarOut.write(resources[s])
            jarOut.closeEntry()
        }
        jarOut.close()
    }
}
