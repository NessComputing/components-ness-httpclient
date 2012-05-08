package com.nesscomputing.httpclient.testing;


import java.io.IOException;

import javax.annotation.concurrent.Immutable;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.HttpClientResponseHandler;

/**
 * Simple {@link HttpClientResponseHandler} which does nothing but expose the internal
 * {@link HttpClientResponse}.  Mostly useful for testing when you want to assert on
 * properties of the HTTP response directly rather than converting it into a domain object.
 */
@Immutable
public class CapturingHttpResponseHandler implements HttpClientResponseHandler<HttpClientResponse> {
    @Override
    public HttpClientResponse handle(HttpClientResponse response)
    throws IOException {
        return response;
    }
}
