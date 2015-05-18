/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which read properties from file.
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@Mojo(name = "classify", defaultPhase = LifecyclePhase.INITIALIZE)
public class ClassifyVersioningMojo extends VersioningMojo {

    /**
     * Property name.
     * This property value will be set to selected version.
     */
    @Parameter(property = "versioning.version.kind.property", defaultValue = "project.version.kind", required = true)
    String versionKindPropertyName = "project.version.kind";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Parsed version is " + version);
        project.getProperties().setProperty(versionKindPropertyName, versionKind(version));
    }
}
