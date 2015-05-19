/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.coreext;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.JAXBException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Checks if all required core extensions are installed
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE)
public class CheckCoreextMojo extends CoreextMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File extensionsFile = new File(new File(rootDirectory, ".mvn"), "extensions.xml");
        Extensions extensionsObject;
        try {
            extensionsObject = unmarshal(extensionsFile);
        } catch (FileNotFoundException ex) {
            extensionsObject = new Extensions();
        } catch (JAXBException ex) {
            throw new MojoFailureException("\nCorrupted .mvn/extensions.xml file.\n\nRun\n    mvn coreext:install\nto overwrite this file", ex);
        }
        if (extensionsObject.extensions == null)
            extensionsObject.extensions = new ArrayList<Extension>();
        ExtensionProcessor processor = new ExtensionProcessor(extensionsObject.extensions);
        List<Extension> uninstalledExtensions = processor.getUninstalled(extensions);
        if (!uninstalledExtensions.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("\nSome required core extensions are not installed:\n\n");
            for (Extension extension: uninstalledExtensions) {
                message.append(" * ").append(extension).append("\n");
            }
            message.append("\nRun\n\n    mvn coreext:install\n\nto install missing core extensions");
            throw new MojoFailureException(message.toString());
        }
        Map<Extension, String> differentInstalledVersions = processor.getDifferentInstalledVersions(this.extensions);
        if (!differentInstalledVersions.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("\nSome installed core extensions have different version than required:\n\n");
            for (Entry<Extension, String> entry: differentInstalledVersions.entrySet()) {
                message.append(" * ").append(entry.getKey().groupId).append(":").append(entry.getKey().artifactId);
                message.append(": ").append(entry.getKey().version).append(" required, but ").append(entry.getValue()).append(" installed");
            }
            message.append("\nRun\n\n    mvn coreext:install\n\nto install correct core extensions versions");
            throw new MojoFailureException(message.toString());
        }
    }
}
