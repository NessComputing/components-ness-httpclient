/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.httpclient.response;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;

import org.apache.commons.io.IOUtils;

/**
 * Base class to get rid of the repetitive "throw the exception out" method.
 */
public abstract class AbstractErrorHandlingContentConverter<T> implements ContentConverter<T>
{
    private static final int MAX_RESPONSE_PRINT_CHARS = 4096;
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

    /**
     * Throw a {@link HttpResponseException} with some basic details about the failing request,
     * and log the first 4k of the response body for diagnostics.
     */
    public static HttpResponseException throwHttpResponseException(HttpClientResponse response)
    throws IOException
    {
        LOG.warn("Remote service responded to \"%s\" with code %d (cause: %s) %s", response.getUri(), response.getStatusCode(), response.getStatusText(), printData(response));
        throw new HttpResponseException(response);
    }

    private static String printData(HttpClientResponse response) throws IOException
    {
        try {
            if (!LOG.isInfoEnabled()) {
                return "(response body available, turn on INFO log to see it)";
            }

            final String charsetName = Objects.firstNonNull(response.getCharset(), Charsets.UTF_8.name());
            final Charset charset;

            try {
                charset = Charset.forName(charsetName);
            } catch (IllegalArgumentException e) {
                LOG.warn(e, "While finding charset '%s'", charsetName);
                return String.format("(invalid charset %s: %s)", charsetName, e);
            }

            Reader in = new InputStreamReader(response.getResponseBodyAsStream(), charset);
            char[] buffer = new char[MAX_RESPONSE_PRINT_CHARS];
            buffer[0] = '\n';
            int len = IOUtils.read(in, buffer, 1, buffer.length - 1);

            // Don't close the reader, we should not close the InputStream, otherwise turning
            // debug on/off changes the behavior...

            return new String(buffer, 0, len + 1);
        } catch (Exception e) {
            // Catching Exception is distasteful, but we really don't want to hide the
            // HttpResponseException that is about to be thrown.
            LOG.error(e, "While reading characters from errored response");
            return String.format("(exception: %s)", e);
        }
    }
}
