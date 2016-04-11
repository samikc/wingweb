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


import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.HttpDestinationFactory;

import java.io.IOException;

@NoJSR250Annotations()
public class NettyHttpDestinationFactory implements HttpDestinationFactory {
    
    public NettyHttpDestinationFactory() {
        io.netty.util.Version.identify();
    }

    public AbstractHTTPDestination createDestination(EndpointInfo endpointInfo, Bus bus,
                                                     DestinationRegistry registry) throws IOException {
        NettyHttpServerEngineFactory serverEngineFactory = bus
                .getExtension(NettyHttpServerEngineFactory.class);
        return new NettyHttpDestination(bus, registry, endpointInfo, serverEngineFactory);
    }

}
