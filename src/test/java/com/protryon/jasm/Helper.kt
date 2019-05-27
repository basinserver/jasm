package com.protryon.jasm

import com.protryon.jasm.stage1.Stage1Class
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.OutputStream
import java.net.URI
import java.util.stream.Collectors
import javax.tools.*

object Helper {

    class JavaFileInternal(name: String, val source: String) : SimpleJavaFileObject(URI.create("string:///" + name.replace('.','/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {

        var output: ByteArray? = null

        private val outStream = object: ByteArrayOutputStream() {
            override fun close() {
                output = this.toByteArray()
                super.close()
            }
        }

        override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
            return source
        }

        override fun openOutputStream(): OutputStream {
            return outStream
        }
    }

    private class FileManagerInternal(parent: JavaFileManager) : ForwardingJavaFileManager<JavaFileManager>(parent) {
        override fun getJavaFileForOutput(p0: JavaFileManager.Location?, p1: String?, p2: JavaFileObject.Kind?, p3: FileObject?): JavaFileObject {
            return p3 as JavaFileInternal
        }
    }

    fun compileTestRaw(name: String, source: String): ByteArray {
        val compiler = ToolProvider.getSystemJavaCompiler()
        val file = JavaFileInternal(name, source)
        compiler.getTask(null, FileManagerInternal(compiler.getStandardFileManager(null, null, null)), null, null, null, arrayListOf(file)).call()
        return file.output!!
    }

    fun compileTestStage1(name: String, source: String): Stage1Class {
        return Stage1Class(DataInputStream(ByteArrayInputStream(compileTestRaw(name, source))), false)
    }

    fun compileKlass(name: String, source: String): Klass {
        val resources = linkedMapOf<String, Resource>()
        resources.put("$name.class", Resource("$name.class", compileTestRaw(name, source), false))
        val classpath = Classpath(resources)
        return classpath.getKlasses().getValue(name)
    }

    fun compileMethod(name: String, source: String): Method {
        return compileKlass("test", "public class test { public void $name() {$source} }").methods.stream().filter { it.name != "<init>" }.collect(Collectors.toList())[0];
    }
}