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


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import com.nesscomputing.httpclient.HttpClientResponse;

import org.apache.commons.io.IOUtils;

/**
 * ContentConverter implementation that exposes the response directly.
 */
public class HttpResponseContentConverter implements ContentConverter<HttpResponse> {
    @Override
    public HttpResponse convert(HttpClientResponse response, InputStream inputStream) throws IOException {
        return new HttpResponse(response.getStatusCode(), IOUtils.toByteArray(inputStream), response.getCharset(), headersFor(response.getAllHeaders()));
    }

    private Multimap<String, String> headersFor(Map<String, List<String>> allHeaders) {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Entry<String, List<String>> e : allHeaders.entrySet()) {
            builder.putAll(e.getKey(), e.getValue());
        }

        return builder.build();
    }

    @Override
    public HttpResponse handleError(HttpClientResponse response, IOException ex) throws IOException {
        throw ex;
    }
}
