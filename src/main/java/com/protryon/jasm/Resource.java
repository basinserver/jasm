package com.protryon.jasm;

public class Resource {

    public final String path;
    public final byte[] content;

    public Resource(String path, byte[] content) {
        this.path = path;
        this.content = content;
    }

}
