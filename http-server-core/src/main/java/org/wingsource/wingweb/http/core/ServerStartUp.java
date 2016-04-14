package org.wingsource.wingweb.http.core;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.wingsource.wingweb.http.services.VersionService;

/**
 * This is the main entry point for the web server. It starts and reads the
 * config files for all JAX-RS services of Apache CXF and sets them up. If
 * there are no services defined in the config file a default version service
 * is added
 *
 * Also it looks into the handler loading for the file processor. Once its
 * started it will start serving the files for WEBROOT directory.
 * Created by samikc on 11/4/16.
 */
public class ServerStartUp {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        initServer();
    }

    public static void initServer() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ServerContext context = ServerContext.getSingleInstance();
        JAXRSServerFactoryBean sf = context.getSf();
        sf.setProvider(new JacksonJsonProvider());
        for (Service service : context.getServiceRegistry().getService()) {
            String className = service.getName();
            Class clazz = context.getClass().getClassLoader().loadClass(className);
            sf.setResourceClasses(clazz);
            sf.setResourceProvider(VersionService.class,
                    new SingletonResourceProvider(clazz.newInstance()));
        }
        sf.setResourceClasses(VersionService.class);
        sf.setResourceProvider(VersionService.class,
                new SingletonResourceProvider(new VersionService()));
        String host = context.getHostName();
        int port = context.getPort();
        String address = "http://"+host+":"+port +"/";
        sf.setAddress(address);

        sf.create();
    }
}
