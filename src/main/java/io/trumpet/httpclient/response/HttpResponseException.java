package io.trumpet.httpclient.response;

import static java.lang.String.format;
import io.trumpet.httpclient.HttpClientResponse;

import java.io.IOException;

public class HttpResponseException extends IOException
{
    private static final long serialVersionUID = 1L;

    private final HttpClientResponse httpResponse;

    public HttpResponseException(final HttpClientResponse httpResponse)
    {
        this.httpResponse = httpResponse;
    }

    public int getStatusCode()
    {
        return httpResponse != null ? httpResponse.getStatusCode() : -1;
    }

    public HttpClientResponse getHttpResponse()
    {
        return httpResponse;
    }

    @Override
    public String toString()
    {
        if (httpResponse == null) {
            return "reason unknown";
        }
        else {
            return format("Status code: %d, Cause: %s", httpResponse.getStatusCode(), httpResponse.getStatusText());
        }
    }
}
