/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.coreext;

import edu.emory.mathcs.backport.java.util.Collections;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class CoreextMojo extends AbstractMojo {
    @Parameter(property = "maven.multiModuleProjectDirectory", readonly = true, required = true)
    File rootDirectory;

    @Parameter
    List<Extension> extensions = Collections.emptyList();

    Extensions unmarshal(File extensionsFile) throws FileNotFoundException, JAXBException {
        InputStream fileStream = new FileInputStream(extensionsFile);
        try {
            InputStream bufferedStream = new BufferedInputStream(fileStream);
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Object result = unmarshaller.unmarshal(bufferedStream);
                return unwrapExtensions(result);
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

    private Extensions unwrapExtensions(Object object) {
        if (object instanceof Extensions)
            return (Extensions)object;
        else if (!(object instanceof JAXBElement))
            throw new ClassCastException(MessageFormat.format("Unable to cast {0} object of {1} type to {2}", object, object.getClass(), Extensions.class));
        else {
            JAXBElement<?> jaxbElement = (JAXBElement<?>)object;
            if (Extensions.class.isAssignableFrom(jaxbElement.getDeclaredType()))
                return (Extensions)jaxbElement.getValue();
            else
                throw new ClassCastException(MessageFormat.format("Unable to cast {0} of {1} type to {2}", jaxbElement.getValue(), jaxbElement.getDeclaredType(), Extensions.class));
        }
    }
}
