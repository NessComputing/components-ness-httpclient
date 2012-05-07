package io.trumpet.httpclient.testing;

import io.trumpet.httpclient.HttpClientRequest;
import io.trumpet.httpclient.HttpClientResponse;

import java.io.IOException;

/**
 * Given a HTTP request, generate a response object.  Meant to imitate the
 * remote service that you are mocking or stubbing out in your testing code.
 */
public interface ResponseGenerator<T> {
    HttpClientResponse respondTo(HttpClientRequest<T> request)
    throws IOException;
}
