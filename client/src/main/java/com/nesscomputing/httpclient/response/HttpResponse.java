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
package com.nesscomputing.httpclient.response;

import java.io.UnsupportedEncodingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/** Simple bean for use in HttpResponseContentConverter. */
@Immutable
public class HttpResponse {
    private final byte[] body;
    @Nullable
    private final String charset;
    private final int statusCode;
    private final Multimap<String, String> headers;

    HttpResponse(int statusCode, @Nonnull byte[] body, @Nullable String charset, @Nonnull Multimap<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.charset = charset;
        this.headers = ImmutableMultimap.copyOf(headers);
    }

    public Multimap<String, String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** @return the body */
    @Nonnull
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_REP")
    public byte[] getBody() {
        return body;
    }

    /**
     * @return the body interpreted as a string. If a charset was returned in the http response, it will be used. If
     *         not, the body will be interpreted as UTF-8.
     * @throws UnsupportedEncodingException if the encoding is unsupported
     */
    @Nonnull
    public String getBodyAsString() throws UnsupportedEncodingException {
        if (charset == null) {
            return new String(body, Charsets.UTF_8);
        }
        return new String(body, charset);
    }
}
