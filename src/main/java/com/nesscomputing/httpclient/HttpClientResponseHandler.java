package com.nesscomputing.httpclient;

import java.io.IOException;

/**
 * A content handler. It parses the response from the server and generates the
 * content object. 
 */
public interface HttpClientResponseHandler<T>
{
    /**
     * Process the {@link HttpClientResponse} object and generate an appropriate
     * response object.
     */
    T handle(HttpClientResponse response) throws IOException;
}
