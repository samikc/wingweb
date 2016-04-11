package org.wingsource.wingweb.http.services;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by samikc on 11/4/16.
 */
@XmlRootElement(name = "Version")
public class Version {
    private String version = null;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
