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
	 * True if the start() method was invoked successfully.
	 */
	boolean isStarted();

    /**
     * True if the stop() method was invoked successfully.
     */
	boolean isStopped();

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

