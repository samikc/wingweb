package org.wingsource.wingweb.http.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Created by samikc on 11/4/16.
 */

@Path("/version/")
@Produces("application/json")
public class VersionService {
    @GET
    @Path("/v/")
    public Version getVersion() {
        System.out.println("----invoking getVersion");
        Version v = new Version();
        v.setVersion("0.1");
        return v;
    }
}
