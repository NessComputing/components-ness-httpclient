package com.nesscomputing.httpclient.testing;


import java.io.IOException;

import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponse;

/**
 * Given a HTTP request, generate a response object.  Meant to imitate the
 * remote service that you are mocking or stubbing out in your testing code.
 */
public interface ResponseGenerator<T> {
    HttpClientResponse respondTo(HttpClientRequest<T> request)
    throws IOException;
}
