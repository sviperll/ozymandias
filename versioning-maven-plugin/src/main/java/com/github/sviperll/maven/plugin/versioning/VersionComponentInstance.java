/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class VersionComponentInstance {
    private final String separator;
    private final VersionComponent component;
    public VersionComponentInstance(String separator, VersionComponent component) {
        this.component = component;
        this.separator = separator;
    }

    VersionComponent component() {
        return component;
    }

    String separator() {
        return separator;
    }

    VersionComponentInstance withTheSameSeparator(VersionComponent newComponent) {
        if (component.defaultSeparator().equals(newComponent.defaultSeparator()))
            return new VersionComponentInstance(separator, newComponent);
        else
            return new VersionComponentInstance(newComponent.defaultSeparator(), newComponent);
    }

    boolean isLessThanOrEqualsToFinal() {
        return component.isLessThanOrEqualsToFinal();
    }

    VersionSchema schema() {
        return component.schema();
    }

    boolean isFinalComponent() {
        return component.isFinalComponent();
    }

    boolean isNumbers() {
        return component.isNumbers();
    }

    @Override
    public String toString() {
        return separator + component.toString();
    }
}
