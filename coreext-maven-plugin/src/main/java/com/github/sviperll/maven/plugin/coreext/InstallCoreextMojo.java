/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.coreext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Install required core extensions are installed
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.INITIALIZE)
public class InstallCoreextMojo extends CoreextMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File mvnDirectory = new File(rootDirectory, ".mvn");
        File extensionsFile = new File(mvnDirectory, "extensions.xml");
        Extensions extensionsObject;
        try {
            extensionsObject = unmarshal(extensionsFile);
        } catch (FileNotFoundException ex) {
            extensionsObject = new Extensions();
        } catch (JAXBException ex) {
            extensionsObject = new Extensions();
        }
        if (extensionsObject.extensions == null)
            extensionsObject.extensions = new ArrayList<Extension>();
        ExtensionProcessor processor = new ExtensionProcessor(extensionsObject.extensions);
        List<Extension> uninstalledExtensions = processor.getUninstalled(extensions);
        Map<Extension, String> differentInstalledVersions = processor.getDifferentInstalledVersions(extensions);
        if (uninstalledExtensions.isEmpty() && differentInstalledVersions.isEmpty()) {
            getLog().info("No core extension installation is nessesary");
        } else {
            processor.install(extensions);
            for (Extension extension: extensionsObject.extensions) {
                getLog().info("Writing " + extension);
            }
            try {
                if (!mvnDirectory.exists()) {
                    boolean created = mvnDirectory.mkdir();
                    if (!created)
                        throw new IOException(MessageFormat.format("Unable to create {0} directory", mvnDirectory));
                }
                if (!mvnDirectory.isDirectory())
                    throw new IOException(MessageFormat.format("Not a directory: {0}", mvnDirectory));
                write(extensionsObject, extensionsFile);
            } catch (IOException ex) {
                throw new MojoExecutionException(MessageFormat.format("Unable to write {0} file", extensionsFile), ex);
            } catch (JAXBException ex) {
                throw new MojoExecutionException(MessageFormat.format("Unable to write {0} file", extensionsFile), ex);
            }
            throw new MojoExecutionException("\n"
                    + "*****\n"
                    + "\n"
                    + "SUCCESS!!!\n"
                    + "It's not an error actually!\n"
                    + "The reason for this showing as an error is that restart of the build is required.\n"
                    + "All core extensions are successfully installed.\n"
                    + "Please restart your build\n"
                    + "\n"
                    + "*****\n"
                    + "\n");
        }
    }

    private void write(Extensions extensionsObject, File extensionsFile) throws IOException, JAXBException {
        OutputStream fileStream = new FileOutputStream(extensionsFile);
        try {
            OutputStream bufferedStream = new BufferedOutputStream(fileStream);
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(extensionsObject, bufferedStream);
            } finally {
                try {
                    bufferedStream.close();
                } catch (Exception ex) {
                    getLog().error("Error closing BufferedInputStream", ex);
                }
            }
        } finally {
            try {
                fileStream.close();
            } catch (Exception ex) {
                getLog().error("Error closing FileInputStream", ex);
            }
        }
    }
}
