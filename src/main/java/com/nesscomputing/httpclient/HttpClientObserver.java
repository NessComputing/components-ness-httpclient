package com.nesscomputing.httpclient;

import java.io.IOException;


/**
 * Allows inspection of HttpClient requests and responses.
 */
public abstract class HttpClientObserver {

    /**
     * Called when a request was submitted to the client. Interception allows setting of
     * headers by wrapping the request passed in (using HttpClientRequest.Builder#fromRequest).
     */
    public <RequestType> HttpClientRequest<RequestType> onRequestSubmitted(final HttpClientRequest<RequestType> request)
        throws IOException
    {
        return request;
    }

    /**
     * Inspect the incoming response.  Called after the HTTP headers have been received,
     * but before the response handler has been invoked.  The response may be inspected, however
     * consuming the response body (via {@link HttpClientResponse#getResponseBodyAsStream()}) would
     * likely break things spectacularly.
     */
    public HttpClientResponse onResponseReceived(final HttpClientResponse response)
        throws IOException
    {
        return response;
    }
}
