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

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientConnectionContext;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.internal.HttpClientBodySource;
import com.nesscomputing.httpclient.internal.HttpClientFactory;
import com.nesscomputing.httpclient.internal.HttpClientHeader;
import com.nesscomputing.logging.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;

/**
 * The actual dispatch logic behind a test {@link HttpClient}.  Implements request matching
 * and delegates to the matched {@link ResponseGenerator}.  The clients returned never need to
 * be {@link #close()}d, although it is not harmful.
 */
@Immutable
class TestingHttpClientFactory implements HttpClientFactory {
    private static final Log LOG = Log.findLog();
    private final HttpClientConnectionContext connectionContext = new TestingHttpClientConnectionContext();
    private final ImmutableMap<RequestMatcher, ResponseGenerator<?>> responseMap;

    TestingHttpClientFactory(ImmutableMap<RequestMatcher, ResponseGenerator<?>> responseMap) {
        LOG.trace("Initializing testing http client... map = %s", responseMap);
        this.responseMap = responseMap;
    }

    @Override
    public HttpClientConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public HttpClientBodySource getHttpBodySourceFor(Object content) {
        return new TestingBodySource(content);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" }) // Type erasure strikes again!
    @Override
    public <T> T performRequest(HttpClientRequest<T> request) throws IOException {
        LOG.trace("Processing request %s", request);
        ResponseGenerator responseGenerator = null;
        for (Entry<RequestMatcher, ResponseGenerator<?>> e : responseMap.entrySet()) {
            if (e.getKey().apply(request)) {
                responseGenerator = e.getValue();
                break;
            }
        }
        if (responseGenerator == null) {
            throw new IllegalStateException("No response matcher found for request " + request);
        }
        LOG.trace("Picked responder %s", responseGenerator);
        HttpClientBodySource httpBodySource = request.getHttpBodySource();
        if (httpBodySource != null) {
            httpBodySource.setContentType(getHeader(request, "Content-Type"));
            httpBodySource.setContentEncoding(getHeader(request, "Content-Encoding"));
        }
        return request.getHttpHandler().handle(responseGenerator.respondTo(request));
    }

    private String getHeader(HttpClientRequest<?> request, final String header) {
        Collection<HttpClientHeader> candidates = Collections2.filter(request.getHeaders(), new Predicate<HttpClientHeader>() {
            @Override
            public boolean apply(HttpClientHeader h) {
                return h.getName().equals(header);
            }
        });
        Preconditions.checkArgument(candidates.size() <= 1, "multiple " + header + " headers");
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.iterator().next().getValue();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    private static class TestingBodySource implements HttpClientBodySource {
        private final Object content;
        private volatile String contentEncoding;
        // Set once upon first use, to simulate the fact that you can only read from a socket once
        private final AtomicReference<InputStream> stream = new AtomicReference<InputStream>();
        public TestingBodySource(Object content) {
            this.content = content;
        }

        @Override
        public void setContentType(String contentType) {
            // The HttpClientBodySource interface is confusing, what am I supposed to do with a Content-Type here?
            // this.contentType = contentType;
        }

        @Override
        public void setContentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
        }

        @Override
        public InputStream getContent() throws IOException {
            if (stream.get() != null) {
                return stream.get();
            }
            if (content instanceof byte[]) {
                stream.compareAndSet(null, new ByteArrayInputStream((byte[]) content));
            } else if (content instanceof String) {
                stream.compareAndSet(null, new ByteArrayInputStream(((String) content).getBytes(ObjectUtils.toString(contentEncoding, "UTF-8"))));
            } else if (content instanceof InputStream) {
                return (InputStream) content;
            } else {
                throw new UnsupportedOperationException("What do you want me to do with a " + content.getClass());
            }
            return stream.get();
        }
    }
}
