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


import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;

import com.nesscomputing.httpclient.internal.HttpClientBodySource;

import java.io.IOException;
import java.io.InputStream;


/**
 * Apache HttpClient4 implementation of {@link HttpClientBodySource}.
 */
final class InternalHttpBodySource implements HttpClientBodySource
{
    private final AbstractHttpEntity httpEntity;

    InternalHttpBodySource(final AbstractHttpEntity httpEntity)
    {
        this.httpEntity = httpEntity;
    }

    @Override
    public void setContentEncoding(final String contentEncoding)
    {
        httpEntity.setContentEncoding(contentEncoding);
    }

    @Override
    public InputStream getContent() throws IOException {
        return httpEntity.getContent();
    }

    @Override
    public void setContentType(final String contentType)
    {
        httpEntity.setContentType(contentType);
    }

    HttpEntity getHttpEntity()
    {
        return httpEntity;
    }
}
