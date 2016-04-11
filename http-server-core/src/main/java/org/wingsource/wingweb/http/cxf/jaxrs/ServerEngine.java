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

import org.apache.cxf.configuration.jsse.TLSServerParameters;

import javax.net.ssl.SSLException;
import java.net.URL;
import java.security.cert.CertificateException;

public interface ServerEngine {

    void addServant(URL url, NettyHttpHandler handler) throws CertificateException, InterruptedException, SSLException;

    /**
     * Remove a previously registered servant.
     *
     * @param url the URL the servant was registered against.
     */
    void removeServant(URL url);

    /**
     * Get a previously  registered servant.
     *
     * @param url the associated URL
     * @return the HttpHandler if registered
     */
    NettyHttpHandler getServant(URL url);

    public void setTlsServerParameters(TLSServerParameters params);

    public void finalizeConfig();

    public String getProtocol();

    public void shutdown();

    public int getPort();

    public int getReadIdleTime();

    public void setReadIdleTime(int readIdleTime);

    public int getWriteIdleTime();

    public void setWriteIdleTime(int writeIdleTime);

}
