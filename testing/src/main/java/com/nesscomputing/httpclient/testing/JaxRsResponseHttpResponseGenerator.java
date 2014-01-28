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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponse;

import org.apache.commons.lang3.ObjectUtils;

/**
 * An all-purpose {@link ResponseGenerator} which supports converting JAX-RS {@link Response}
 * objects into the actual {@link HttpClientResponse} that you might expect to see.
 * <p>Supported entity formats include:
 * <ul>
 * <li> Null: empty <code>text/plain</code> response
 * <li> String: <code>text/plain</code> response
 * <li> Byte array: <code>application/octet-stream</code> response
 * <li> Object: <code>application/json</code> response generated via Jackson's {@link ObjectMapper}
 * </ul>
 * The above Content-Type is set unless the Response already has one, which always takes precedence.
 * The Content-Length header is always present.  The charset is always <code>UTF-8</code>.
 */
@Immutable
public class JaxRsResponseHttpResponseGenerator implements ResponseGenerator<Object> {
    private final Response response;
    private final InputStream responseBody;
    private final MediaType contentType;
    private final long contentLength;
    private final String charset;
    /**
     * Create a new response generator
     * @param mapper Jackson mapper for converting entities to JSON, may be null.  Attempts to
     * serialize to JSON with a null mapper will fail spectacularly.
     * @param response The response to send back on fetches.
     */
    public JaxRsResponseHttpResponseGenerator(@Nullable ObjectMapper mapper, Response response) {
        this.response = response;
        Object entity = response.getEntity();
        charset = Charsets.UTF_8.name();
        MediaType contentTypeIn = null;
        List<Object> typeHeaders = response.getMetadata().get("Content-Type");
        if (typeHeaders == null) { // Damn you to hell, people who reinvent Multimap poorly!
            typeHeaders = Collections.emptyList();
        }
        Preconditions.checkArgument(typeHeaders.size() <= 1, "multiple Content-Type headers?!");
        final byte[] encoded;
        if (!typeHeaders.isEmpty()) {
            contentTypeIn = MediaType.valueOf(typeHeaders.get(0).toString());
        }
        if (entity == null) {
            if (contentTypeIn == null) {
                contentTypeIn = MediaType.TEXT_PLAIN_TYPE;
            }
            encoded = new byte[0];
        } else if (entity instanceof String) {
            if (contentTypeIn == null) {
                contentTypeIn = MediaType.TEXT_PLAIN_TYPE;
            }
            encoded = ((String) entity).getBytes(Charsets.UTF_8);
        } else if (entity instanceof byte[]) {
            if (contentTypeIn == null) {
                contentTypeIn = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            }
            encoded = (byte[]) entity;
        } else {
            Preconditions.checkNotNull(mapper, "Mapper null and unknown type " + entity.getClass() + " provided");
            if (contentTypeIn == null) {
                contentTypeIn = MediaType.APPLICATION_JSON_TYPE;
            }
            try {
                encoded = mapper.writeValueAsBytes(entity);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        responseBody = new ByteArrayInputStream(encoded);
        contentLength = encoded.length;
        this.contentType = contentTypeIn;
    }

    @Override
    public HttpClientResponse respondTo(final HttpClientRequest<Object> request) {
        return new HttpClientResponse() {
            @Override
            public int getStatusCode() {
                return response.getStatus();
            }
            @Override
            public String getStatusText() {
                return Status.fromStatusCode(getStatusCode()).getReasonPhrase();
            }
            @Override
            public InputStream getResponseBodyAsStream() throws IOException {
                return responseBody;
            }

            @Override
            public URI getUri() {
                return request.getUri();
            }

            @Override
            public String getContentType() {
                return ObjectUtils.toString(contentType, null);
            }

            @Override
            public Long getContentLength() {
                return contentLength;
            }

            @Override
            public String getCharset() {
                return charset;
            }

            @Override
            public String getHeader(String name) {
                List<Object> headers = response.getMetadata().get(name);
                if (headers == null || headers.isEmpty()) {
                    return null;
                }
                return headers.get(0).toString();
            }

            @Override
            public List<String> getHeaders(String name) {
                List<Object> headers = response.getMetadata().get(name);
                if (headers == null) {
                     return null;
                }
                return ImmutableList.copyOf(Collections2.transform(headers, Functions.toStringFunction()));
            }

            @Override
            public Map<String, List<String>> getAllHeaders() {
                ImmutableMap.Builder<String, List<String>> result = ImmutableMap.builder();
                for (Entry<String, List<Object>> e : response.getMetadata().entrySet()) {
                    result.put(e.getKey(), ImmutableList.copyOf(Collections2.transform(e.getValue(), Functions.toStringFunction())));
                }
                return result.build();
            }

            @Override
            public boolean isRedirected() {
                return false;
            }
        };
    }

    @Override
    public String toString() {
        return String.format("JaxRsResponseHttpResponseGenerator" +
                " [status=%d %s, contentType=%s, contentLength=%s, charset=%s]",
                        response.getStatus(), Status.fromStatusCode(response.getStatus()).getReasonPhrase(),
                        contentType, contentLength, charset);
    }
}
