package org.wingsource.wingweb.http.core;

import com.google.gson.Gson;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by samikc on 11/4/16.
 */
public class ServerContext {

    private static final Logger LOG = Logger.getLogger(ServerContext.class.getName());

    private static ServerContext SINGLE_INSTANCE;
    private static int port = 9000; // By default port is 9000
    private static String hostName = "localhost"; // By default host name is localhost
    private static String WEBROOT = "WEBROOT/"; // By default webroot folder is WEBROOT/
    private final JAXRSServerFactoryBean sf;
    private ServiceRegistry serviceRegistry;

    private ServerContext() {

        sf = new JAXRSServerFactoryBean();
        init();

    }

    public static ServerContext getSingleInstance() {
        if (SINGLE_INSTANCE == null) {
            SINGLE_INSTANCE = new ServerContext();
        }
        return SINGLE_INSTANCE;
    }

    synchronized private void init() {
        // load all configurations properties
        setupProperties();
        // load all services in service_registry.json
        loadServices();
    }


    private void setupProperties() {
        try {
            InputStream wingWebPropertiesStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("wingweb.properties");
            Properties wingWebProperties = new Properties();
            wingWebProperties.load(wingWebPropertiesStream);
            this.hostName = wingWebProperties.getProperty("HOST") != null ? wingWebProperties.getProperty("HOST") : "localhost";
            this.port = (Integer)(wingWebProperties.get("PORT") == null ? 9000 :  Integer.parseInt(wingWebProperties.getProperty("PORT")));
        } catch (IOException e) {
            LOG.warning("Unable to load wingweb.properties file in the classpath");
        }
    }

    private void loadServices() {
        InputStream wingWebServicesJsonStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("services.json");
        InputStreamReader reader = new InputStreamReader(wingWebServicesJsonStream);
        Gson gson = new Gson();
        serviceRegistry = gson.fromJson(reader, ServiceRegistry.class);
    }



    public JAXRSServerFactoryBean getSf() {
        return sf;
    }

    public static int getPort() {
        return port;
    }

    public static String getHostName() {
        return hostName;
    }

    public static String getWEBROOT() {
        return WEBROOT;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}
