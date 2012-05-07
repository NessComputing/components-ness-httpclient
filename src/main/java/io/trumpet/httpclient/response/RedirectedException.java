package io.trumpet.httpclient.response;

import io.trumpet.httpclient.HttpClientResponse;

/**
 * Indicates a redirection response from a server.
 */
public class RedirectedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final String url;

    /**
     * Creates a new Redirection execption.
     */
    public RedirectedException(final int statusCode, final String url)
    {
        super();
        this.statusCode = statusCode;
        this.url = url;
    }

    /**
     * Creates a new Redirection execption with message.
     */
    public RedirectedException(final int statusCode, final String url, final String message)
    {
        super(message);
        this.statusCode = statusCode;
        this.url = url;
    }

    /**
     * Creates a new Redirection execption from a {@link HttpClientResponse}.
     */
    public RedirectedException(final HttpClientResponse response)
    {
        this(response.getStatusCode(), response.getHeader("Location"));
    }

    /**
     * Creates a new Redirection execption from a {@link HttpClientResponse} with message.
     */
    public RedirectedException(final HttpClientResponse response, final String message)
    {
        this(response.getStatusCode(), response.getHeader("Location"), message);
    }

    /**
     * Status code of the response that caused this exception.
     */
    public int getStatusCode()
    {
        return statusCode;
    }

    /**
     * URI of the response that caused this exception.
     */
    public String getUrl()
    {
        return url;
    }
}
