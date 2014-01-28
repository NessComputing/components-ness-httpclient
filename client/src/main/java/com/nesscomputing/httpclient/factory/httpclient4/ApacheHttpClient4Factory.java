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
package com.nesscomputing.httpclient.factory.httpclient4;


import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;

import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;

import com.nesscomputing.httpclient.HttpClientAuthProvider;
import com.nesscomputing.httpclient.HttpClientConnectionContext;
import com.nesscomputing.httpclient.HttpClientDefaults;
import com.nesscomputing.httpclient.HttpClientObserver;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.internal.AlwaysTrustServerTrustManager;
import com.nesscomputing.httpclient.internal.HttpClientBodySource;
import com.nesscomputing.httpclient.internal.HttpClientFactory;
import com.nesscomputing.httpclient.internal.HttpClientHeader;
import com.nesscomputing.httpclient.internal.HttpClientTrustManagerFactory;
import com.nesscomputing.httpclient.internal.MultiTrustManager;
import com.nesscomputing.logging.Log;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/** Apache HttpClient4 based implementation of {@link HttpClientFactory}. */
public class ApacheHttpClient4Factory implements HttpClientFactory
{
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;

    private static final Scheme HTTP_SCHEME =
        new Scheme("http", HTTP_PORT, PlainSocketFactory.getSocketFactory());

    private static final Log LOG = Log.findLog();

    private final SchemeRegistry registry = new SchemeRegistry();

    private final ThreadSafeClientConnManager connectionManager;
    private final InternalConnectionContext connectionContext = new InternalConnectionContext();

    private boolean started = false;
    private boolean stopped = false;

    // HttpClient state
    private final HttpParams params = new BasicHttpParams();
    private volatile int retries = 3;

    private volatile long idleTimeout = 0;
    private volatile IdleTimeoutThread idleTimeoutThread = null;

    private final Set<? extends HttpClientObserver> httpClientObservers;
    private final String defaultAcceptEncoding;

    public ApacheHttpClient4Factory(final HttpClientDefaults clientDefaults,
                                    @Nullable final Set<? extends HttpClientObserver> httpClientObservers)
    {
        Preconditions.checkArgument(clientDefaults != null, "clientDefaults can not be null!");

        this.httpClientObservers = httpClientObservers;

        initParams();

        registry.register(HTTP_SCHEME);

        try {
            final TrustManager[] trustManagers = new TrustManager [] { getTrustManager(clientDefaults) };
            final KeyManager[] keyManagers = getKeyManagers(clientDefaults);

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            final SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext);

            registry.register(new Scheme("https", HTTPS_PORT, sslSocketFactory));
        } catch (GeneralSecurityException ce) {
            throw new IllegalStateException(ce);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }

        connectionManager = new ThreadSafeClientConnManager(registry);

        defaultAcceptEncoding = StringUtils.trimToNull(clientDefaults.getDefaultAcceptEncoding());
    }

    /**
     * @param clientDefaults defaults to read from
     * @return a trust manager
     * @throws GeneralSecurityException if the crypto goes wrong
     * @throws IOException            if trust keystore can't be loaded
     */
    @Nonnull
    private static TrustManager getTrustManager(HttpClientDefaults clientDefaults)
        throws GeneralSecurityException, IOException
    {
        X509TrustManager trustManager;

        if (clientDefaults.getSSLTruststore() == null || clientDefaults.getSSLTruststorePassword() == null) {
            LOG.trace("Not using custom truststore");
            trustManager = HttpClientTrustManagerFactory.getDefaultTrustManager();
        } else {
            LOG.trace("Using custom truststore at %s", clientDefaults.getSSLTruststore());
            final MultiTrustManager multiTrustManager = new MultiTrustManager();

            if (clientDefaults.useSSLTruststoreFallback()) {
                LOG.trace("Adding fallback to default trust manager");
                multiTrustManager.addTrustManager(HttpClientTrustManagerFactory.getDefaultTrustManager());
            }
            multiTrustManager.addTrustManager(HttpClientTrustManagerFactory.getTrustManagerForHttpClientDefaults(clientDefaults));

            trustManager = multiTrustManager;
        }

        if (!clientDefaults.useSSLServerCertVerification()) {
            LOG.trace("Server cert checking disabled");
            trustManager = new AlwaysTrustServerTrustManager(trustManager);
        }
        return trustManager;
    }

    /**
     * @param clientDefaults defaults to read from
     * @return key manager array to use in SSLContext, or null if no custom trust manager is needed. If
     *         non-null, it will contain one X509KeyManager.
     * @throws GeneralSecurityException if the crypto goes wrong
     * @throws IOException            if client keystore can't be loaded
     */
    @CheckForNull
    private static KeyManager[] getKeyManagers(HttpClientDefaults clientDefaults)
        throws IOException, GeneralSecurityException {
        if (clientDefaults.getSSLKeystore() == null ||
            clientDefaults.getSSLKeystoreType() == null ||
            clientDefaults.getSSLKeystorePassword() == null) {
            return null;
        }

        final KeyManager manager = HttpClientTrustManagerFactory.getKeyManager(clientDefaults.getSSLKeystore(),
                                                                               clientDefaults.getSSLKeystoreType(),
                                                                               clientDefaults.getSSLKeystorePassword());
        return new KeyManager[] { manager };

    }

    /**
     * Expose the observer set.  Only for testing.
     */
    protected Set<? extends HttpClientObserver> getHttpClientObservers()
    {
        return httpClientObservers;
    }

    @Override
    public void start()
    {
        if (!started && !stopped) {
            if (idleTimeout > 0) {
                startIdleTimeoutThread();
            }

            started = true;
            LOG.debug("Apache HTTPClient4 based factory running.");
        }
    }

    @Override
    public void stop()
    {
        if (started && !stopped) {
            stopped = true;

            stopIdleTimeoutThread();
            connectionManager.shutdown();

            LOG.debug("Factory stopped.");
        }
    }

    @Override
    public boolean isStarted()
    {
        return started;
    }

    @Override
    public boolean isStopped()
    {
        return stopped;
    }

    @Override
    public HttpClientConnectionContext getConnectionContext() {
        // Can be called even if the factory is not yet running.
        return connectionContext;
    }

    @Override
    public HttpClientBodySource getHttpBodySourceFor(final Object content) {
        checkRunning();

        if (content == null) {
            LOG.debug("No content given, returning null");
            return null;
        }

        if (content instanceof String) {
            LOG.debug("Returning String based body source.");
            return new InternalHttpBodySource(new BetterStringEntity(String.class.cast(content), Charsets.UTF_8));
        } else if (content instanceof byte[]) {
            LOG.debug("Returning byte array based body source.");
            return new InternalHttpBodySource(new ByteArrayEntity((byte[]) content));
        } else if (content instanceof InputStream) {
            LOG.debug("Returning InputStream based body source.");
            return new InternalHttpBodySource(new InputStreamEntity((InputStream) content, -1));
        }

        return null;
    }

    @Override
    public <T> T performRequest(final HttpClientRequest<T> incomingRequest) throws IOException {
        checkRunning();

        HttpClientRequest<T> request = incomingRequest;

        if (CollectionUtils.isNotEmpty(httpClientObservers)) {
            LOG.trace("Executing Observers");
            for (HttpClientObserver observer : httpClientObservers) {
                request = observer.<T>onRequestSubmitted(request);
            }

            if (request != incomingRequest) {
                LOG.trace ("Request was modified by Observers!");
            }
        }

        request = contributeAcceptEncoding(request);

        LOG.trace("Got a '%s' request", request.getHttpMethod());

        switch (request.getHttpMethod()) {
        case DELETE:
            return executeRequest(new HttpDelete(request.getUri()), request);

        case HEAD:
            return executeRequest(new HttpHead(request.getUri()), request);

        case OPTIONS:
            return executeRequest(new HttpOptions(request.getUri()), request);

        case POST:
            final HttpPost httpPost = new HttpPost(request.getUri());
            final HttpClientBodySource postSource = request.getHttpBodySource();

            if (postSource instanceof InternalHttpBodySource) {
                httpPost.setEntity(((InternalHttpBodySource) postSource).getHttpEntity());
            }
            return executeRequest(httpPost, request);

        case PUT:
            final HttpPut httpPut = new HttpPut(request.getUri());
            final HttpClientBodySource putSource = request.getHttpBodySource();

            if (putSource instanceof InternalHttpBodySource) {
                httpPut.setEntity(((InternalHttpBodySource) putSource).getHttpEntity());
            }
            return executeRequest(httpPut, request);

        case GET:
            return executeRequest(new HttpGet(request.getUri()), request);

        default:
            LOG.warn("Got an unknown request type: '%s', falling back to GET",
                request.getHttpMethod());
            return executeRequest(new HttpGet(request.getUri()), request);
        }
    }

    private void initParams() {
        params.setBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, true);
        params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
    }

    private void checkRunning() {
        if (!started || stopped) {
            throw new IllegalStateException("Factory was not started!");
        }
    }

    private void stopIdleTimeoutThread() {
        if (idleTimeoutThread != null) {
            LOG.debug("Stopping Idle Timeout Thead");

            idleTimeoutThread.shutdown();
            idleTimeoutThread = null;
        }
    }

    private void startIdleTimeoutThread() {
        stopIdleTimeoutThread();

        idleTimeoutThread = new IdleTimeoutThread();
        idleTimeoutThread.start();

        LOG.debug("Started Idle Timeout Thread with '%d' idle timeout", this.idleTimeout);
    }

    private <T> T executeRequest(final HttpRequestBase httpRequest,
        final HttpClientRequest<T> httpClientRequest) throws IOException {
        final DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, setFollowRedirects(params, httpClientRequest));
        httpClient.getCookieSpecs().register(NessCookieSpecFactory.NESS_COOKIE_POLICY, new NessCookieSpecFactory());
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retries, false));

        contributeCookies(httpClient, httpClientRequest);

        contributeParameters(httpClient, httpRequest, httpClientRequest);

        contributeHeaders(httpRequest, httpClientRequest);

        contributeVirtualHost(httpRequest, httpClientRequest);

        contributeAuthentication(httpClient, httpClientRequest);

        try {
            final HttpContext httpContext = new BasicHttpContext();
            final HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);

            final HttpClientResponseHandler<T> responseHandler = httpClientRequest.getHttpHandler();

            try {
                final HttpClientResponse internalResponse = new InternalResponse(httpRequest, httpResponse);
                HttpClientResponse response = internalResponse;

                if (CollectionUtils.isNotEmpty(httpClientObservers)) {
                    LOG.trace("Executing Observers");
                    for (HttpClientObserver observer : httpClientObservers) {
                        response = observer.onResponseReceived(response);
                    }

                    if (response != internalResponse) {
                        LOG.trace("Response was modified by Observers!");
                    }
                }

                if (responseHandler != null) {
                    LOG.trace("Executing Response Handler");
                    return responseHandler.handle(response);
                } else {
                    LOG.debug("No response handler found, discarding response.");
                    return null;
                }
            } finally {
                // Make sure that the content has definitely been consumed. Otherwise,
                // keep-alive does not work.
                EntityUtils.consume(httpResponse.getEntity());
            }
        } catch (IOException ioe) {
            LOG.debug(ioe, "Aborting Request!");
            httpRequest.abort();
            throw ioe;
        } catch (RuntimeException re) {
            LOG.debug(re, "Aborting Request!");
            httpRequest.abort();
            throw re;
        }
    }

    private <T> void contributeCookies(final DefaultHttpClient httpClient,
        final HttpClientRequest<T> httpClientRequest) {
        final List<Cookie> cookies = httpClientRequest.getCookies();

        if (CollectionUtils.isNotEmpty(cookies)) {
            final CookieStore cookieStore = new BasicCookieStore();
            for (final Cookie cookie : cookies) {
                final BasicClientCookie httpCookie =
                    new BasicClientCookie(cookie.getName(), cookie.getValue());

                final int maxAge = cookie.getMaxAge();

                if (maxAge > 0) {
                    final Date expire = new Date(System.currentTimeMillis() + maxAge * 1000L);
                    httpCookie.setExpiryDate(expire);
                    httpCookie.setAttribute(ClientCookie.MAX_AGE_ATTR, Integer.toString(maxAge));
                }

                httpCookie.setVersion(1);
                httpCookie.setPath(cookie.getPath());
                httpCookie.setDomain(cookie.getDomain());
                httpCookie.setSecure(cookie.getSecure());

                LOG.debug("Adding cookie to the request: '%s'", httpCookie);
                cookieStore.addCookie(httpCookie);
            }
            httpClient.setCookieStore(cookieStore);
        } else {
            LOG.debug("No cookies found.");
            httpClient.setCookieStore(null);
        }
    }

    private <T> void contributeParameters(final DefaultHttpClient httpClient,
                                          final HttpRequestBase httpRequest,
                                          final HttpClientRequest<T> httpClientRequest)
    {
        final Map<String, Object> parameters = httpClientRequest.getParameters();

        if (parameters != null && !parameters.isEmpty()) {
            HttpParams clientParams = httpClient.getParams();
            HttpParams requestParams = httpRequest.getParams();

            for (Map.Entry<String, Object> entry: parameters.entrySet()) {
                clientParams.setParameter(entry.getKey(), entry.getValue());
                requestParams.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    private <T> void contributeHeaders(final HttpRequestBase httpRequest,
        HttpClientRequest<T> httpClientRequest) {
        final String virtualHost = httpClientRequest.getVirtualHost();
        final List<HttpClientHeader> headers = httpClientRequest.getHeaders();
        if (CollectionUtils.isNotEmpty(headers)) {
            for (final HttpClientHeader httpHeader : headers) {
                final String headerName = httpHeader.getName();

                // Don't add Host headers, let the virtual host setting do its trick.
                if (virtualHost == null || !"host".equals(headerName.toLowerCase(Locale.ENGLISH))) {

                    LOG.debug("Adding Header '%s' : '%s' to the request", headerName,
                        httpHeader.getValue());

                    httpRequest.addHeader(headerName, httpHeader.getValue());
                }
            }
        }
    }

    private <T> void contributeVirtualHost(final HttpRequestBase httpRequest,
        final HttpClientRequest<T> httpClientRequest) {
        final String virtualHost = httpClientRequest.getVirtualHost();
        if (StringUtils.isNotBlank(virtualHost)) {

            LOG.debug("Adding Virtual Host: '%s/%d'", virtualHost,
                httpClientRequest.getVirtualPort());

            httpRequest.getParams().setParameter(ClientPNames.VIRTUAL_HOST,
                new HttpHost(virtualHost, httpClientRequest.getVirtualPort()));
        }
    }

    private <T> void contributeAuthentication(final DefaultHttpClient httpClient,
        final HttpClientRequest<T> httpClientRequest) {
        final List<HttpClientAuthProvider> authProviders = httpClientRequest.getAuthProviders();
        if (CollectionUtils.isNotEmpty(authProviders)) {
            httpClient.setCredentialsProvider(new InternalCredentialsProvider(authProviders));
        }
    }

    private <T> HttpClientRequest<T> contributeAcceptEncoding(HttpClientRequest<T> request)
    {
        if (defaultAcceptEncoding == null) {
            return request;
        }

        for (HttpClientHeader h : request.getHeaders()) {
            if (StringUtils.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING, h.getName())) {
                return request;
            }
        }

        return HttpClientRequest.Builder.fromRequest(request).addHeader(HttpHeaders.ACCEPT_ENCODING, defaultAcceptEncoding).request();
    }

    private <T> HttpParams setFollowRedirects(final HttpParams params,
        final HttpClientRequest<T> httpClientRequest) {
        Boolean followRedirects = httpClientRequest.followRedirects();
        if (followRedirects != null) {
            return params.copy().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, followRedirects);
        }
        return params;
    }

    private class InternalConnectionContext implements HttpClientConnectionContext {
        private InternalConnectionContext() {
        }

        @Override
        public void setSocketTimeout(final long socketTimeout) {
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, (int) socketTimeout);
        }

        @Override
        public void setConnectionTimeout(final long connTimeout) {
            params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, (int) connTimeout);
        }

        @Override
        public void setFollowRedirects(final boolean followRedirects) {
            params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, followRedirects);
        }

        @Override
        public void setIdleTimeout(final long idleTimeout) {
            ApacheHttpClient4Factory.this.idleTimeout = idleTimeout;
        }

        @Override
        public void setMaxRedirects(final int maxRedirects) {
            params.setIntParameter(ClientPNames.MAX_REDIRECTS, maxRedirects);
        }

        @Override
        public void setPerHostConnectionsMax(final int perHostConnectionsMax) {
            connectionManager.setDefaultMaxPerRoute(perHostConnectionsMax);
        }

        @Override
        public void setRequestTimeout(final long reqTimeout) {
            // TODO: what should this set?
        }

        @Override
        public void setTotalConnectionsMax(final int totalConnectionsMax) {
            connectionManager.setMaxTotal(totalConnectionsMax);
        }

        @Override
        public void setUserAgent(final String userAgent) {
            params.setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
        }

        @Override
        public void setRetries(final int retries) {
            ApacheHttpClient4Factory.this.retries = retries;
        }
    }

    /** Manages idle and expired connections. Straight from http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e550 */
    private class IdleTimeoutThread extends Thread {
        private volatile boolean shutdown = false;

        private static final long magicWaitTime = 5000L;

        private IdleTimeoutThread() {
            setName("ApacheHttpClient4Factory IdleTimeout");
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(magicWaitTime);

                        // Close expired connections
                        connectionManager.closeExpiredConnections();

                        // Expire idle connections
                        connectionManager.closeIdleConnections(idleTimeout, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }


        @edu.umd.cs.findbugs.annotations.SuppressWarnings("NN_NAKED_NOTIFY")
        public void shutdown() {
            shutdown = true;
            this.interrupt();
            synchronized (this) {
                this.notifyAll();
            }
        }
    }
}
