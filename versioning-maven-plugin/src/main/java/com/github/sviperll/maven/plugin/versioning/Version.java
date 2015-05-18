/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class Version implements Comparable<Version> {

    static Version of(VersionComponentInstance... instances) {
        StringBuilder builder = new StringBuilder();
        for (VersionComponentInstance instance: instances) {
            builder.append(instance.toString());
        }
        return new Version(builder.toString());
    }
    private final String versionString;
    Version(String versionString) {
        this.versionString = versionString;
    }

    @Override
    public int compareTo(Version that) {
        VersionComponentScanner thisScanner = VersionComponentScanner.createInstance(this.versionString);
        VersionComponentScanner thatScanner = VersionComponentScanner.createInstance(that.versionString);
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
        return new Version(versionString + extention.toString());
    }
}
