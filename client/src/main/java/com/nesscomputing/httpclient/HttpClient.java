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


import java.io.Closeable;
import java.net.URI;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.nesscomputing.httpclient.factory.httpclient4.ApacheHttpClient4Factory;
import com.nesscomputing.httpclient.internal.HttpClientFactory;
import com.nesscomputing.httpclient.internal.HttpClientMethod;


/**
 * Connection pooling and management to access a remote service over HTTP.
 */
public final class HttpClient implements Closeable
{
    private final HttpClientFactory httpClientFactory;

    /**
     * Creates a new HTTP client with the default implementation (currently Apache HTTPClient 4) and default settings.
     */
    public HttpClient()
    {
        this(new HttpClientDefaults());
    }


    /**
     * Creates a new HTTP client with the default implementation (currently Apache HTTPClient 4).
	 *
	 * @param defaults the defaults to use
	 */
    public HttpClient(final HttpClientDefaults defaults)
    {
        this(new ApacheHttpClient4Factory(defaults, null));
    }

    /**
     * Creates a new HTTP client with the default implementation (currently Apache HTTPClient 4) and a bunch of observers.
     *
     * @param defaults the defaults to use
     */
    public HttpClient(final HttpClientDefaults defaults, final Set<? extends HttpClientObserver> observers)
    {
        this(new ApacheHttpClient4Factory(defaults, observers));
    }

    /**
     * Creates a new HTTP client with a custom HttpClientFactory and default settings.
	 * @param httpClientFactory factory to use
	 */
    public HttpClient(final HttpClientFactory httpClientFactory)
    {
        this(httpClientFactory, new HttpClientDefaults());
    }


    /**
     * Creates a new HTTP Client.
     */
    public HttpClient(@Nonnull final HttpClientFactory httpClientFactory,
                      @Nonnull final HttpClientDefaults httpClientDefaults)
    {
        Preconditions.checkArgument(httpClientFactory != null, "http client factory can not be null!");
        Preconditions.checkArgument(httpClientDefaults != null, "http client defaults can not be null!");

        this.httpClientFactory = httpClientFactory;
        setDefaults(httpClientDefaults);
    }


    public HttpClient start()
    {
        httpClientFactory.start();
        return this;
    }

    public HttpClient stop()
    {
        httpClientFactory.stop();
        return this;
    }

    public boolean isStarted()
    {
            return httpClientFactory.isStarted();
    }

    public boolean isStopped()
    {
            return httpClientFactory.isStopped();
    }

    /**
     * Close the HTTP client. This releases all resources. After closing, no new requests can be created.
     */
    @Override
    public void close()
    {
        stop();
    }

    private void setDefaults(final HttpClientDefaults httpClientDefaults)
    {
        final HttpClientConnectionContext context = getConnectionContext();
        if (context != null) {
            context.setConnectionTimeout(httpClientDefaults.getConnectionTimeout().getMillis());
            context.setSocketTimeout(httpClientDefaults.getSocketTimeout().getMillis());
            context.setFollowRedirects(httpClientDefaults.isFollowRedirects());
            context.setIdleTimeout(httpClientDefaults.getIdleTimeout().getMillis());
            context.setMaxRedirects(httpClientDefaults.getMaxRedirects());
            context.setPerHostConnectionsMax(httpClientDefaults.getPerHostConnectionsMax());
            context.setRequestTimeout(httpClientDefaults.getRequestTimeout().getMillis());
            context.setRetries(httpClientDefaults.getRetries());
            context.setTotalConnectionsMax(httpClientDefaults.getTotalConnectionsMax());
            context.setUserAgent(httpClientDefaults.getUserAgent());
        }
    }

    /**
	 * @return the connection context for this client. This contains settings such as timeout, number of retries etc.
	 */
    public HttpClientConnectionContext getConnectionContext()
    {
        return httpClientFactory.getConnectionContext();
    }

    /**
     * Start building a GET request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> get(final URI uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return new HttpClientRequest.Builder<T>(httpClientFactory, HttpClientMethod.GET, uri, httpHandler);
    }

    /**
     * Start building a GET request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> get(final String uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return get(URI.create(uri), httpHandler);
    }

    /**
     * Start building a HEAD request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> head(final URI uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return new HttpClientRequest.Builder<T>(httpClientFactory, HttpClientMethod.HEAD, uri, httpHandler);
    }

    /**
     * Start building a HEAD request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> head(final String uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return head(URI.create(uri), httpHandler);
    }

    /**
     * Start building a DELETE request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> delete(final URI uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return new HttpClientRequest.Builder<T>(httpClientFactory, HttpClientMethod.DELETE, uri, httpHandler);
    }

    /**
     * Start building a DELETE request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> delete(final String uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return delete(URI.create(uri), httpHandler);
    }

    /**
     * Start building a OPTIONS request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> options(final URI uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return new HttpClientRequest.Builder<T>(httpClientFactory, HttpClientMethod.OPTIONS, uri, httpHandler);
    }

    /**
     * Start building a OPTIONS request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> options(final String uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return options(URI.create(uri), httpHandler);
    }

    /**
     * Start building a PUT request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> put(final URI uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return new HttpClientRequest.Builder<T>(httpClientFactory, HttpClientMethod.PUT, uri, httpHandler);
    }

    /**
     * Start building a PUT request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> put(final String uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return put(URI.create(uri), httpHandler);
    }

    /**
     * Start building a POST request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> post(final URI uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return new HttpClientRequest.Builder<T>(httpClientFactory, HttpClientMethod.POST, uri, httpHandler);
    }

    /**
     * Start building a POST request.
     *
     *  @return a request builder which then can be used to create the actual request.
     */
    public <T> HttpClientRequest.Builder<T> post(final String uri, final HttpClientResponseHandler<T> httpHandler)
    {
        return post(URI.create(uri), httpHandler);
    }

    /**
     * Start building a request with a user-supplied HTTP method (of the standard HTTP verbs)
     */
    public <T> HttpClientRequest.Builder<T> perform(final String methodName, final URI uri, final HttpClientResponseHandler<T> httpHandler) {
        final HttpClientMethod method;
        try {
            method = HttpClientMethod.valueOf(methodName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown HTTP method type " + methodName, e);
        }

        return new HttpClientRequest.Builder<T>(httpClientFactory, method, uri, httpHandler);
    }
}
