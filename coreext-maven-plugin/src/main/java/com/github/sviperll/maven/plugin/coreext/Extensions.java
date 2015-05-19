package com.github.sviperll.maven.plugin.coreext;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author vir
 */

@XmlRootElement(name = "extensions")
@XmlAccessorType(XmlAccessType.NONE)
public class Extensions {
    @XmlElement(name = "extension")
    List<Extension> extensions;
}
