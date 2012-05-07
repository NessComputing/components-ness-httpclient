package io.trumpet.httpclient.testing;

import io.trumpet.httpclient.HttpClientConnectionContext;

import javax.annotation.concurrent.Immutable;

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
