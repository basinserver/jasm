package com.protryon.jasm;

public class Resource {

    public final String path;
    public final byte[] content;
    public final boolean isLibraryResource;

    public Resource(String path, byte[] content, boolean isLibraryResource) {
        this.path = path;
        this.content = content;
        this.isLibraryResource = isLibraryResource;
    }

}
