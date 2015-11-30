/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class Version implements Comparable<Version> {

    static Version of(VersionSchema schema, VersionComponentInstance... instances) {
        StringBuilder builder = new StringBuilder();
        for (VersionComponentInstance instance: instances) {
            if (schema != instance.schema())
                throw new IllegalStateException("Comparison of version component with different suffix schema");
            builder.append(instance.toString());
        }
        return new Version(schema, builder.toString());
    }
    private final VersionSchema schema;
    private final String versionString;
    Version(VersionSchema schema, String versionString) {
        this.schema = schema;
        this.versionString = versionString;
    }

    @Override
    public int compareTo(Version that) {
        if (this.schema != that.schema)
            throw new IllegalStateException("Comparison of version component with different suffix schema");
        VersionComponentScanner thisScanner = VersionComponentScanner.createInstance(schema, this.versionString);
        VersionComponentScanner thatScanner = VersionComponentScanner.createInstance(schema, that.versionString);
        while (thisScanner.hasMoreComponents() || thatScanner.hasMoreComponents()) {
            VersionComponent thisComponent = thisScanner.getNextComponentInstance().component();
            VersionComponent thatComponent = thatScanner.getNextComponentInstance().component();

            int result = thisComponent.compareTo(thatComponent);
            if (result != 0)
                return result;
        }
        return 0;
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        else if (!(thatObject instanceof Version))
            return false;
        else {
            Version that = (Version)thatObject;
            return this.compareTo(that) == 0;
        }
    }

    @Override
    public int hashCode() {
        return versionString.hashCode();
    }

    @Override
    public String toString() {
        return versionString;
    }

    Version extended(VersionComponentInstance extention) {
        if (extention.component().isSuffix() && this.schema != extention.schema())
            throw new IllegalStateException("Comparison of version component with different suffix schema");
        return new Version(schema, versionString + extention.toString());
    }

    Version simpleExtention() {
        return extended(schema.numbersComponentInstance("", new int[] {1}));
    }
}
