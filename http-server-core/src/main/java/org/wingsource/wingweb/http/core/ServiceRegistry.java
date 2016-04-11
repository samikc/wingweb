package org.wingsource.wingweb.http.core;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by samikc on 11/4/16.
 */
public class ServiceRegistry {
    List<Service> service = new ArrayList<Service>();

    public List<Service> getService() {
        return service;
    }

    public void setService(List<Service> service) {
        this.service = service;
    }
}
