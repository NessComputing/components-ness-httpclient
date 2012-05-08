/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.httpclient;

/**
 * Holds various tuneable parameters for a http client.
 */
public interface HttpClientConnectionContext
{
    /**
     * The maximum number of connections allowed.
     */
    void setTotalConnectionsMax(int totalConnectionsMax);

    /**
     * The number of connections allowed to a single host/port combination.
     */
    void setPerHostConnectionsMax(int perHostConnectionsMax);

    /**
     * Timeout connecting to a remote host in milliseconds.
     */
    void setConnectionTimeout(long connTimeout);

    /**
     * Timeout for an established connection in keep-alive until it is closed. If 0, connections are closed immediately.
     */
    void setIdleTimeout(long idleTimeout);


    /** Timeout for a connection to receive data.
     * @param socketTimeout  Milliseconds
     */

    void setSocketTimeout(long socketTimeout);
    /**
     * Timeout for a request sent until a response must arrive.
     */
    void setRequestTimeout(long reqTimeout);

    /**
     * If true, follow redirects automatically.
     */
    void setFollowRedirects(boolean followRedirects);

    /**
     * Maximum number of redirects until an error is returned.
     */
    void setMaxRedirects(int maxRedirects);

    /**
     * Name of the user agent, used to identify to remote sites.
     */
    void setUserAgent(String userAgent);

    /**
     * Maximum number of retries for a request.
     */
    void setRetries(int retries);
}

