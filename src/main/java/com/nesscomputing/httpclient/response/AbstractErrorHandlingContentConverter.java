package com.nesscomputing.httpclient.response;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

import java.io.IOException;

/**
 * Base class to get rid of the repetitive "throw the exception out" method.
 */
public abstract class AbstractErrorHandlingContentConverter<T> implements ContentConverter<T>
{
    private static final Log LOG = Log.findLog();

    @Override
    public T handleError(final HttpClientResponse httpClientResponse, final IOException ex)
        throws IOException
    {
        if (httpClientResponse != null) {
            LOG.trace("Failure cause is: %s", httpClientResponse.getStatusText());
        }
        throw ex;
    }
}
