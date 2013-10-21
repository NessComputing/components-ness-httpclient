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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import net.jpountz.lz4.LZ4BlockInputStream;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.io.SizeExceededException;
import com.nesscomputing.httpclient.io.SizeLimitingInputStream;
import com.nesscomputing.logging.Log;


/**
 * A generic content response handler for the Http Client. It handles all cases of redirect, compressed responses etc.
 */
public class ContentResponseHandler<T> implements HttpClientResponseHandler<T>
{
    private static final Log LOG = Log.findLog();

    private final ContentConverter<T> contentConverter;
    private final int maxBodyLength;
    private final boolean allowRedirect;

    public static <CC> ContentResponseHandler<CC> forConverter(final ContentConverter<CC> contentConverter)
    {
        return new ContentResponseHandler<CC>(contentConverter);
    }

    /**
     * Creates a new ContentResponseHandler. It accepts unlimited data and will not follow redirects.
     *
     * @param contentConverter The content converter to use to convert the response into the reply object.
     */
    public ContentResponseHandler(final ContentConverter<T> contentConverter)
    {
        this(contentConverter, -1, false);
    }

    /**
     * Creates a new ContentResponseHandler. It will not follow redirects.
     *
     * @param contentConverter The content converter to use to convert the response into the reply object.
     * @param maxBodyLength The maximum number of bytes to read from the server. -1 means 'unlimited'.
     *
     * @throws SizeExceededException When the body length is bigger than maxBodyLength.
     */
    public ContentResponseHandler(final ContentConverter<T> contentConverter, final int maxBodyLength)
    {
        this(contentConverter, maxBodyLength, false);
    }

    /**
     * Creates a new ContentResponseHandler.
     *
     * @param contentConverter The content converter to use to convert the response into the reply object.
     * @param maxBodyLength The maximum number of bytes to read from the server. -1 means 'unlimited'.
     * @param allowRedirect If true, the handler will throw a {@link RedirectedException} to signal redirection to the caller.
     *
     * @throws SizeExceededException When the body length is bigger than maxBodyLength.
     * @throws RedirectedException When the server returned a 3xx return code.
     */
    public ContentResponseHandler(final ContentConverter<T> contentConverter, final int maxBodyLength, final boolean allowRedirect)
    {
        this.contentConverter = contentConverter;
        this.maxBodyLength = maxBodyLength;
        this.allowRedirect = allowRedirect;
    }

    /**
     * Processes the client response.
     */
    @Override
    public T handle(final HttpClientResponse response) throws IOException
    {
        if(allowRedirect && response.isRedirected()) {
            LOG.debug("Redirecting based on '%d' response code", response.getStatusCode());
            throw new RedirectedException(response);
        } else {
            // Find the response stream - the error stream may be valid in cases
            // where the input stream is not.
            InputStream is = null;
            try {
                is = response.getResponseBodyAsStream();
            }
            catch (IOException e) {
                LOG.warnDebug(e, "Could not locate response body stream");
                // normal for 401, 403 and 404 responses, for example...
            }

            if (is == null) {
                // Fall back to zero length response.
                is = new NullInputStream(0);
            }

            try {
                final Long contentLength = response.getContentLength();

                if (maxBodyLength > 0) {
                    if (contentLength != null && contentLength > maxBodyLength) {
                        throw new SizeExceededException("Content-Length: " + contentLength);
                    }

                    LOG.debug("Limiting stream length to '%d'", maxBodyLength);
                    is = new SizeLimitingInputStream(is, maxBodyLength);
                }

                final String encoding = StringUtils.trimToEmpty(response.getHeader("Content-Encoding"));

                if (StringUtils.equalsIgnoreCase(encoding, "lz4")) {
                    LOG.debug("Found LZ4 stream");
                    is = new LZ4BlockInputStream(is);
                } else if (StringUtils.equalsIgnoreCase(encoding, "gzip") || StringUtils.equalsIgnoreCase(encoding, "x-gzip")) {
                    LOG.debug("Found GZIP stream");
                    is = new GZIPInputStream(is);
                }
                else if (StringUtils.equalsIgnoreCase(encoding, "deflate")) {
                    LOG.debug("Found deflate stream");
                    final Inflater inflater = new Inflater(true);
                    is = new InflaterInputStream(is, inflater);
                }

                return contentConverter.convert(response, is);
            }
            catch (IOException ioe) {
                return contentConverter.handleError(response, ioe);
            }
        }
    }
}
