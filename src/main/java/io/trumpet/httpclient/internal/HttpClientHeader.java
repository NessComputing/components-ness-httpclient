package io.trumpet.httpclient.internal;

import javax.annotation.concurrent.Immutable;

/**
 * Describes a HTTP header.
 */
@Immutable
public class HttpClientHeader
{
    private final String name;
    private final String value;

    /**
     * Create a new header.
	 * @param name header name
	 * @param value header value
	 */
    public HttpClientHeader(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the header name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the header value.
     */
    public String getValue()
    {
        return value;
    }
}
