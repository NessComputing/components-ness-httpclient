package com.nesscomputing.httpclient.internal;

import java.io.IOException;
import java.io.InputStream;

/**
 * Describes a body source for POST and PUT requests.
 *
 * TODO this interface is ugly. Can we get rid of it?
 */
public interface HttpClientBodySource
{
    void setContentType(String contentType);

    void setContentEncoding(String contentEncoding);

    InputStream getContent() throws IOException;
}
