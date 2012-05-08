package com.nesscomputing.httpclient.response;


import java.io.IOException;
import java.io.InputStream;

import com.nesscomputing.httpclient.HttpClientResponse;


/**
 * Convert an Input Stream and content type into a result object.
 */
public interface ContentConverter<T>
{
    /**
     * Called when the ContentResponseHandler wants to convert an input stream
     * into a response object. Calling this method does *not* imply a 2xx response code,
     * this method will be called for all response codes if no error occurs while processing
     * the response.
     *
     * @param response The response object from the Http client.
     * @param inputStream The response body as stream.
     * @return The result object. Can be null.
     * @throws IOException
     */
    T convert(HttpClientResponse response, InputStream inputStream) throws IOException;

    /**
     * Called if an exception occured while trying to convert the response data stream
     * into a response object.
     *
     * @param response The response object from the Http client.
     * @param ex Exception triggering this call.
     * @return A response object or null.
     * @throws IOException
     */
    T handleError(HttpClientResponse response, IOException ex) throws IOException;
}
