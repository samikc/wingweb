/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wingsource.wingweb.http.cxf.jaxrs;


import org.wingsource.wingweb.http.server.WingwebServer;
import org.apache.cxf.Bus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.transport.http.netty.server.ThreadingParameters;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


@NoJSR250Annotations(unlessNull = "bus")
public class NettyHttpServerEngineFactory implements BusLifeCycleListener {
    private static final Logger LOG =
            LogUtils.getL7dLogger(org.apache.cxf.transport.http.netty.server.NettyHttpServerEngineFactory.class);

    private static ConcurrentHashMap<Integer, ServerEngine> portMap =
            new ConcurrentHashMap<Integer, ServerEngine>();

    private Bus bus;

    private BusLifeCycleManager lifeCycleManager;
    
    /**
     * This map holds the threading parameters that are to be applied
     * to new Engines when bound to the reference id.
     */
    private Map<String, ThreadingParameters> threadingParametersMap =
        new TreeMap<String, ThreadingParameters>();
    
    private Map<String, TLSServerParameters> tlsServerParametersMap =
        new TreeMap<String, TLSServerParameters>();

    public NettyHttpServerEngineFactory() {
        // Empty
    }
    
    public NettyHttpServerEngineFactory(Bus b) {
        setBus(b);
    }
    
    public NettyHttpServerEngineFactory(Bus b,
                                        Map<String, TLSServerParameters> tls,
                                        Map<String, ThreadingParameters> threads) {
        setBus(b);
        tlsServerParametersMap = tls;
        threadingParametersMap = threads;
    }

    /**
     * This call is used to set the bus. It should only be called once.
     *
     * @param bus
     */
    @Resource(name = "cxf")
    public final void setBus(Bus bus) {
        this.bus = bus;
        if (bus != null) {
            bus.setExtension(this, NettyHttpServerEngineFactory.class);
            lifeCycleManager = bus.getExtension(BusLifeCycleManager.class);
            if (null != lifeCycleManager) {
                lifeCycleManager.registerLifeCycleListener(this);
            }
        }
    }

    public Bus getBus() {
        return bus;
    }


    public Map<String, TLSServerParameters> getTlsServerParametersMap() {
        return tlsServerParametersMap;
    }

    public void setTlsServerParameters(Map<String, TLSServerParameters> tlsParametersMap) {
        this.tlsServerParametersMap = tlsParametersMap;
    }
    
    public Map<String, ThreadingParameters> getThreadingParametersMap() {
        return threadingParametersMap;
    }
    
    public void setThreadingParametersMap(Map<String, ThreadingParameters> parameterMap) {
        this.threadingParametersMap = parameterMap;
    }
    
    public void setEnginesList(List<ServerEngine> enginesList) {
        for (ServerEngine engine : enginesList) {
            portMap.putIfAbsent(engine.getPort(), engine);
        }    
    }
    

    public void initComplete() {
        // do nothing here
    }

    public void postShutdown() {
        // shut down the Netty server in the portMap
        // To avoid the CurrentModificationException,
        // do not use portMap.values directly
     /*   ServerEngine[] engines = portMap.values().toArray(new NettyHttpServerEngine[portMap.values().size()]);
        for (NettyHttpServerEngine engine : engines) {
            engine.shutdown();
        }*/
        // The engine which is in shutdown status cannot be started anymore
        portMap.clear();
        threadingParametersMap.clear();
        tlsServerParametersMap.clear();
    }

    public void preShutdown() {
        // do nothing here
        // just let server registry to call the server stop first
    }

    private static ServerEngine getOrCreate(NettyHttpServerEngineFactory factory,
                                            String host,
                                            int port,
                                            TLSServerParameters tlsParams
                                                     ) throws IOException {

        ServerEngine ref = portMap.get(port);
        System.out.println("STARTING SERVER FROM HERE");
        if (ref == null) {
            // Here we load netty-http-server.properties file and check if a
            // NETTY_HTTP_SERVER property is provided. If provided we load the
            // class mentioned in the property else load the basic property

            final Properties properties = new Properties();
            try (final InputStream stream =
                         factory.getClass().getClassLoader().getResourceAsStream("netty-http-server.properties")) {
                properties.load(stream);
                String clazz = (String) properties.get("NETTY_HTTP_SERVER");
                Constructor ctor = factory.getClass().getClassLoader().loadClass(clazz).getDeclaredConstructor(String.class,Integer.class);
                ref = (ServerEngine) ctor.newInstance(host,port);
            } catch (Exception e) {
                ref = new WingwebServer(host, port);
            }


            if (tlsParams != null) {
                ref.setTlsServerParameters(tlsParams);
            }
            ref.finalizeConfig();
            ServerEngine tmpRef = portMap.putIfAbsent(port, ref);
            if (tmpRef != null) {
                ref = tmpRef;
            }
        }
        return ref;
    }


    public synchronized ServerEngine retrieveNettyHttpServerEngine(int port) {
        return portMap.get(port);
    }


    public synchronized ServerEngine createNettyHttpServerEngine(String host, int port,
                                                                 String protocol) throws IOException {
        LOG.log(Level.FINE, "CREATING_NETTY_SERVER_ENGINE",  port);
        TLSServerParameters tlsServerParameters = null;
        if (protocol.equals("https") && tlsServerParametersMap != null) {
            tlsServerParameters = tlsServerParametersMap.get(Integer.toString(port));
        }
        ServerEngine ref = getOrCreate(this, host, port, tlsServerParameters);
        // checking the protocol
        if (!protocol.equals(ref.getProtocol())) {
            throw new IOException("Protocol mismatch for port " + port + ": "
                    + "engine's protocol is " + ref.getProtocol()
                    + ", the url protocol is " + protocol);
        }


        return ref;
    }

    public synchronized ServerEngine createNettyHttpServerEngine(int port,
                                                                 String protocol) throws IOException {
        return createNettyHttpServerEngine(null, port, protocol);
    }

    /**
     * This method removes the Server Engine from the port map and stops it.
     */
    public static synchronized void destroyForPort(int port) {
        ServerEngine ref = portMap.remove(port);
        if (ref != null) {
            LOG.log(Level.FINE, "STOPPING_NETTY_SERVER_ENGINE", port);
            try {
                ref.shutdown();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

   
}
