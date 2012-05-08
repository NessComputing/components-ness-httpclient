package com.nesscomputing.httpclient.internal;


import java.io.IOException;

import javax.annotation.CheckForNull;

import com.nesscomputing.httpclient.HttpClientConnectionContext;
import com.nesscomputing.httpclient.HttpClientRequest;


/**
 * API for HTTP Client implementations.
 */
public interface HttpClientFactory
{
    /**
     * Start the Factory. Must be called before any other method can be used.
     */
    void start();

    /**
     * Stop the Factory. Should free all resources and shut down all connections. After stop() has been called,
     * the factory should throw exceptions on all related method calls.
     */
	void stop();

    /**
     * @return a {@link HttpClientConnectionContext} object to modify settings for this factory.
     */
    HttpClientConnectionContext getConnectionContext();

    /**
     * For requests that accept a body, generate a {@link HttpClientBodySource} object that wraps
     * the content object. Can return null if no appropriate body source is available.
     */
	@CheckForNull
    HttpClientBodySource getHttpBodySourceFor(Object content);

    /**
     * Execute a request to a remote server.
     */
    <T> T performRequest(HttpClientRequest<T> request) throws IOException;
}

