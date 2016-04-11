package org.wingsource.wingweb.http.server;

import org.wingsource.wingweb.http.cxf.jaxrs.NettyHttpContextHandler;
import org.wingsource.wingweb.http.cxf.jaxrs.NettyHttpHandler;
import org.wingsource.wingweb.http.cxf.jaxrs.NettyHttpServletPipelineFactory;
import org.wingsource.wingweb.http.cxf.jaxrs.ServerEngine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.transport.HttpUriMapper;
import org.apache.cxf.transport.http.netty.server.ThreadingParameters;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Created by samikc on 10/4/16.
 */
public class WingwebServer implements ServerEngine {

    private static final Logger LOG =
            LogUtils.getL7dLogger(WingwebServer.class);

    public static final String protocol = "http";
    private Channel serverChannel;
    private TLSServerParameters tlsServerParameters;
    private ThreadingParameters threadingParameters = new ThreadingParameters();
    private int readIdleTime = 60;

    private int writeIdleTime = 30;

    // TODO need to setup configuration about them
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();


    private NettyHttpServletPipelineFactory servletPipeline;

    private int maxChunkContentSize = 1048576;
    private boolean sessionSupport;
    static final boolean SSL = System.getProperty("ssl") != null;
    private List<String> registedPaths = new CopyOnWriteArrayList<String>();
    private Map<String, NettyHttpContextHandler> handlerMap = new ConcurrentHashMap<String, NettyHttpContextHandler>();


    private String host = null;
    private int port = 9000;
    public WingwebServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Channel startServer() throws CertificateException, SSLException, InterruptedException {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true);

        // Set up the event pipeline factory.
        servletPipeline =
                new NettyHttpServletPipelineFactory(
                        tlsServerParameters, sessionSupport,
                        threadingParameters.getThreadPoolSize(),
                        maxChunkContentSize,
                        handlerMap, this);
        // Start the servletPipeline's timer
        servletPipeline.start();
        bootstrap.childHandler(servletPipeline);
        InetSocketAddress address = null;
        if (host == null) {
            address = new InetSocketAddress(port);
        } else {
            address = new InetSocketAddress(host, port);
        }
        // Bind and start to accept incoming connections.
        try {
            return bootstrap.bind(address).sync().channel();
        } catch (InterruptedException ex) {
            // do nothing here
            return null;
        }    }

    protected void checkRegistedContext(URL url) {
        String path = url.getPath();
        for (String registedPath : registedPaths) {
            if (path.equals(registedPath)) {
                // Throw the address is already used exception
                throw new Fault(new Message("ADD_HANDLER_CONTEXT_IS_USED_MSG", LOG, url, registedPath));
            }
        }

    }

    @Override
    public void addServant(URL url, NettyHttpHandler handler) throws CertificateException, InterruptedException, SSLException {

        checkRegistedContext(url);
        // need to set the handler name for looking up
        handler.setName(url.getPath());
        String contextName = HttpUriMapper.getContextName(url.getPath());
        // need to check if the NettyContext is there
        NettyHttpContextHandler contextHandler = handlerMap.get(contextName);
        if (contextHandler == null) {
            contextHandler = new NettyHttpContextHandler(contextName);
            handlerMap.put(contextName, contextHandler);
        }
        contextHandler.addNettyHttpHandler(handler);
        registedPaths.add(url.getPath());
        if (serverChannel == null) {
            serverChannel = startServer();
        }

    }

    @Override
    public void removeServant(URL url) {

    }

    @Override
    public NettyHttpHandler getServant(URL url) {
        return null;
    }

    @Override
    public void setTlsServerParameters(TLSServerParameters params) {
        tlsServerParameters = params;
    }

    @Override
    public void finalizeConfig() {

    }

    @Override
    public String getProtocol() {
        return  protocol;
    }

    @Override
    public void shutdown() {

    }

    public boolean isSessionSupport() {
        return sessionSupport;
    }

    public void setSessionSupport(boolean session) {
        this.sessionSupport = session;
    }

    @Override
    public int getPort() {
        return port;
    }

    public int getReadIdleTime() {
        return readIdleTime;
    }

    public void setReadIdleTime(int readIdleTime) {
        this.readIdleTime = readIdleTime;
    }

    public int getWriteIdleTime() {
        return writeIdleTime;
    }

    public void setWriteIdleTime(int writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
    }

}
