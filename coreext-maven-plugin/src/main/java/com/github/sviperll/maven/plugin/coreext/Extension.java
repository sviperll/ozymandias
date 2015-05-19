package com.github.sviperll.maven.plugin.coreext;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author vir
 */

@XmlAccessorType(XmlAccessType.NONE)
public class Extension {
    @XmlElement
    String groupId;

    @XmlElement
    String artifactId;

    @XmlElement
    String version;

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        else if (!(thatObject instanceof Extension))
            return false;
        else {
            Extension that = (Extension)thatObject;
            return this.groupId.trim().equals(that.groupId.trim())
                    && this.artifactId.trim().equals(that.artifactId.trim());
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.groupId.trim().hashCode();
        hash = 29 * hash + this.artifactId.trim().hashCode();
        return hash;
    }
}
