package com.github.sviperll.maven.plugin.coreext;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 * @author vir
 */

@XmlRegistry
public class ObjectFactory {
    static final QName EXTENSIONS_QNAME = new QName("extensions");
    static final QName EXTENSION_QNAME = new QName("extension");

    @XmlElementDecl(namespace = "", name = "extensions")
    public JAXBElement<Extensions> createExtensions(Extensions value) {
        return new JAXBElement<Extensions>(EXTENSIONS_QNAME, Extensions.class, value);
    }    

    @XmlElementDecl(namespace = "", name = "extension")
    public JAXBElement<Extension> createExtension(Extension value) {
        return new JAXBElement<Extension>(EXTENSION_QNAME, Extension.class, value);
    }    
}
