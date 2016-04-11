package org.wingsource.wingweb.sample;

import org.wingsource.wingweb.http.core.ServerStartUp;

/**
 * Created by samikc on 11/4/16.
 */
public class Server {

    public static void main(String[] args) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        ServerStartUp.initServer();
    }

}
