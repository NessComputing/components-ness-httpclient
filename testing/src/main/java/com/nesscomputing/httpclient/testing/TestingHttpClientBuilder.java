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


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientObserver;
import com.nesscomputing.httpclient.internal.HttpClientMethod;

/**
 * Builder of {@link HttpClient} objects which send out canned responses instead
 * of accessing a remote service.  The returned client will dispatch requests with
 * priority given to earlier matches (i.e. in the order you call {@link #whenMatches(RequestMatcher)} or
 * {@link #on(HttpClientMethod)}.  The first matching entry will invoke the attached {@link ResponseGenerator}
 * and produce a full HTTP response.
 */
@NotThreadSafe
public class TestingHttpClientBuilder {
    public interface BuilderWithMethod {
        /**
         * Match any request with the given path
         * @param path the exact path to match, excluding query string
         */
        BuilderWithMatcher of(String path);
    }
    public interface BuilderWithMatcher {
        /**
         * Respond with a given JAX-RS {@link Response}.
         * @see JaxRsResponseHttpResponseGenerator
         */
        void respondWith(ResponseBuilder response);
        /**
         * Full custom control of the response given.
         */
        void respondWith(ResponseGenerator<?> responseGenerator);
        /** Respond by having the generated HttpClientRequest throw the
         * supplied IOException.
         * @param e exception to throw; not null.
         */
        void respondWith(IOException e);
    }

    private final ImmutableMap.Builder<RequestMatcher, ResponseGenerator<?>> requestMap = ImmutableMap.builder();
    /**
     * Used for generating JSON responses; may be null - the {@link JaxRsResponseHttpResponseGenerator} will
     * use this and report any configuration problems much later on if a mapper is required but not provided.
     * May be injected or directly set via {@link #withObjectMapper(ObjectMapper)}
     */
    private ObjectMapper mapper;
    private final Set<HttpClientObserver> observers = new HashSet<>();

    public TestingHttpClientBuilder() { }

    /**
     * General purpose constructor
     * @param mapper Jackson JSON mapper which may be null to disable automagic JSON support.
     * @deprecated use the nullary constructor and a call to {@link #withObjectMapper} instead
     */
    @Deprecated
    public TestingHttpClientBuilder(@Nullable ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Inject(optional = true)
    public TestingHttpClientBuilder withObjectMapper(ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    /**
     * Build the {@link HttpClient}.  The builder is unchanged.
     * @return a new HttpClient.  The returned client is immutable or thread safe
     * if and only if all provided request matchers and response generators are.
     */
    public HttpClient build() {
        return new HttpClient(new TestingHttpClientFactory(requestMap.build(), observers));
    }

    /**
     * Match with a fully customizable {@link RequestMatcher}
     */
    public BuilderWithMatcher whenMatches(final RequestMatcher matcher) {
        return new BuilderWithMatcher() {
            @Override
            public void respondWith(ResponseBuilder response) {
                respondWith(new JaxRsResponseHttpResponseGenerator(mapper, response.build()));
            }
            @Override
            public void respondWith(IOException e) {
                respondWith(new ExceptionResponseGenerator(e));
            }

            @Override
            public void respondWith(ResponseGenerator<?> responseGenerator) {
                requestMap.put(matcher, responseGenerator);
            }
        };
    }

    /**
     * Match on a given HTTP method.
     * @see HttpClientMethod
     */
    public BuilderWithMethod on(final HttpClientMethod method) {
        return new BuilderWithMethod() {
            @Override
            public BuilderWithMatcher of(String path) {
                return whenMatches(new MethodPathMatcher(method, path));
            }
        };
    }

    /**
     * Match on a given HTTP method.
     * @see HttpClientMethod
     */
    public BuilderWithMethod onRegex(final HttpClientMethod method) {
        return new BuilderWithMethod() {
            @Override
            public BuilderWithMatcher of(String path) {
                return whenMatches(new RegexPathMatcher(method, path));
            }
        };
    }

    public TestingHttpClientBuilder withObserver(HttpClientObserver observer)
    {
        observers.add(observer);
        return this;
    }
}
