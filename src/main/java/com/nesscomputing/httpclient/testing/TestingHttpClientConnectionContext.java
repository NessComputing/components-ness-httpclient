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
package com.nesscomputing.httpclient.testing;


import javax.annotation.concurrent.Immutable;

import com.nesscomputing.httpclient.HttpClientConnectionContext;

/**
 * Simple {@link HttpClientConnectionContext} which does nothing at all, since the tester
 * does not actually create any HTTP connections.
 */
@Immutable
class TestingHttpClientConnectionContext implements HttpClientConnectionContext {
    @Override
    public void setSocketTimeout(long socketTimeoutMillis) {}
    @Override
    public void setTotalConnectionsMax(int totalConnectionsMax) { }
    @Override
    public void setPerHostConnectionsMax(int perHostConnectionsMax) { }
    @Override
    public void setConnectionTimeout(long connTimeout) { }
    @Override
    public void setIdleTimeout(long idleTimeout) { }
    @Override
    public void setRequestTimeout(long reqTimeout) { }
    @Override
    public void setFollowRedirects(boolean followRedirects) { }
    @Override
    public void setMaxRedirects(int maxRedirects) { }
    @Override
    public void setUserAgent(String userAgent) { }
    @Override
    public void setRetries(int retries) { }
}
