/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.util.Properties;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which read properties from file.
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@Mojo(name = "update-file", defaultPhase = LifecyclePhase.INITIALIZE, inheritByDefault = false, aggregator = true)
public class UpdateFileVersioningMojo extends VersioningMojo {

    /**
     * Property name.
     * This property value will be set to selected version.
     */
    @Parameter(required = true)
    VersionFile versionFile = new VersionFile();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (versionFile.file == null)
            throw new MojoFailureException(MessageFormat.format("File property should be defined for version file",
                                                                versionFile.type));
        if (!versionFile.type.equals("properties"))
            throw new MojoFailureException(MessageFormat.format("Unsupported version file type: {0}: only 'properties' type is supported for now",
                                                                versionFile.type));
        if (!(versionFile.stability.defaultStability.equals("stable")
              || versionFile.stability.defaultStability.equals("unstable")
                || versionFile.stability.defaultStability.equals("none")))
            throw new MojoFailureException(MessageFormat.format("Unsupported default stability: {0}: only 'stable' and 'unstable' are allowed",
                                                                versionFile.stability));
        Charset charset;
        try {
            charset = Charset.forName(versionFile.encoding);
        } catch (IllegalCharsetNameException ex) {
            throw new MojoFailureException("Illegal encoding name: " + versionFile.encoding, ex);
        } catch (UnsupportedCharsetException ex) {
            throw new MojoFailureException("Unsupported encoding: " + versionFile.encoding, ex);
        }
        try {
            Properties properties;
            try {
                properties = readProperties(versionFile.file, charset);
            } catch (FileNotFoundException ex) {
                properties = new Properties();
            }
            if (isStable(version, versionFile.stability)) {
                properties.remove("version.unstable");
                properties.setProperty("version.stable", version);
            } else {
                properties.setProperty("version.unstable", version);
            }
            if (propertiesChanged(properties, versionFile.file, charset))
                writeProperties(properties, versionFile.file, charset);
        } catch (UndefinedStability ex) {
            throw new MojoExecutionException("Unknown version stability", ex);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error updating version file", ex);
        }
    }

    private boolean isStable(String baseVersion, Stability stability) throws UndefinedStability {
        String kind = versionKind(baseVersion);
        for (String unstableKind: stability.unstableKinds) {
            if (kind.equals(unstableKind))
                return false;
        }
        for (String stableKind: stability.stableKinds) {
            if (kind.equals(stableKind))
                return true;
        }
        if (stability.defaultStability.equals("stable")) {
            return true;
        } else if (stability.defaultStability.equals("unstable")) {
            return false;
        } else
            throw new UndefinedStability("Undefined stability: " + kind);
    }

    private boolean propertiesChanged(Properties newProperties, File file, Charset charset) throws IOException {
        Properties oldProperties;
        try {
            oldProperties = readProperties(file, charset);
        } catch (FileNotFoundException ex) {
            return true;
        }
        if (oldProperties.size() != newProperties.size())
            return true;
        else {
            for (String name: newProperties.stringPropertyNames()) {
                String oldValue = oldProperties.getProperty(name);
                String newValue = newProperties.getProperty(name);
                boolean newIsNull = newValue == null;
                boolean oldIsNull = oldValue == null;
                if (newIsNull != oldIsNull
                    || newValue != null && oldValue != null && !oldValue.equals(newValue)) {
                    return true;
                }
            }
            return false;
        }
    }
}
