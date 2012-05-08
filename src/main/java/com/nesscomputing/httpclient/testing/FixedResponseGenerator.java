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
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponse;

public class FixedResponseGenerator implements ResponseGenerator<String>
{
    private int statusCode = 200;
    private String statusText = "";
    private String contentType = "text/plain";
    private String charset = "utf-8";
    private boolean redirected = false;
    private URI uri = null;

    private long contentLength;
    private final Map<String, List<String>> allHeaders = Maps.newHashMap();

    private final InputStream inputStream;


    public FixedResponseGenerator(final String content)
    {
        this(content.getBytes(Charsets.UTF_8));
    }

    public FixedResponseGenerator(final byte [] buffer)
    {
        this.inputStream = new ByteArrayInputStream(buffer);
        this.contentLength = buffer.length;
    }

    public FixedResponseGenerator setStatusCode(final int statusCode)
    {
        this.statusCode = statusCode;
        return this;
    }

    public FixedResponseGenerator setStatusText(final String statusText)
    {
        this.statusText = statusText;
        return this;
    }

    public FixedResponseGenerator setUri(final URI uri)
    {
        this.uri = uri;
        return this;
    }

    public FixedResponseGenerator setContentType(final String contentType)
    {
        this.contentType = contentType;
        return this;
    }

    public FixedResponseGenerator setCharset(final String charset)
    {
        this.charset = charset;
        return this;
    }

    public FixedResponseGenerator chunked()
    {
        this.contentLength = -1;
        return this;
    }

    public FixedResponseGenerator addHeader(final String key, final String value)
    {
        List<String> headers = allHeaders.get(key);
        if (headers == null) {
            headers = Lists.newArrayList();
            allHeaders.put(key, headers);
        }
        headers.add(value);

        return this;
    }


    @Override
    public HttpClientResponse respondTo(HttpClientRequest<String> request) throws IOException
    {
        return new HttpClientResponse() {

            @Override
            public int getStatusCode()
            {
                return statusCode;
            }

            @Override
            public String getStatusText()
            {
                return statusText;
            }

            @Override
            public InputStream getResponseBodyAsStream() throws IOException
            {
                return inputStream;
            }

            @Override
            public URI getUri()
            {
                return uri;
            }

            @Override
            public String getContentType()
            {
                return contentType;
            }

            @Override
            public Long getContentLength()
            {
                return contentLength;
            }

            @Override
            public String getCharset()
            {
                return charset;
            }

            @Override
            public String getHeader(String name)
            {
                final List<String> headers = allHeaders.get(name);
                if (headers != null) {
                    return headers.get(0);
                }
                else {
                    return null;
                }
            }

            @Override
            public List<String> getHeaders(String name)
            {
                return allHeaders.get(name);
            }

            @Override
            public Map<String, List<String>> getAllHeaders()
            {
                return allHeaders;
            }

            @Override
            public boolean isRedirected()
            {
                return redirected;
            }
        };
    }
}
