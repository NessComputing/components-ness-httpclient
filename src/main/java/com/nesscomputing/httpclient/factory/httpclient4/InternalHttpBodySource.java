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
