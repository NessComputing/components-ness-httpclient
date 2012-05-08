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
import java.io.InputStream;

import javax.annotation.concurrent.Immutable;

/**
 * Returns true for a 200/201/204 response code, false otherwise. Can be set to explode if response code != 200/201/204.
 */
@Immutable
public class Valid2xxContentConverter extends AbstractErrorHandlingContentConverter<Boolean>
{
    public static final Valid2xxContentConverter DEFAULT_CONVERTER = new Valid2xxContentConverter(false);
    public static final Valid2xxContentConverter DEFAULT_FAILING_CONVERTER = new Valid2xxContentConverter(true);
    public static final Valid2xxContentConverter DEFAULT_404OK_CONVERTER = new Valid2xxContentConverter(true, true);

    public static final ContentResponseHandler<Boolean> DEFAULT_RESPONSE_HANDLER = ContentResponseHandler.forConverter(DEFAULT_CONVERTER);
    public static final ContentResponseHandler<Boolean> DEFAULT_FAILING_RESPONSE_HANDLER = ContentResponseHandler.forConverter(DEFAULT_FAILING_CONVERTER);
    public static final ContentResponseHandler<Boolean> DEFAULT_404OK_RESPONSE_HANDLER = ContentResponseHandler.forConverter(DEFAULT_404OK_CONVERTER);

    private static final Log LOG = Log.findLog();

    private final boolean failOnError;
    private final boolean ignore404;

    protected Valid2xxContentConverter(final boolean failOnError)
    {
        this(failOnError, false);
    }

    protected Valid2xxContentConverter(final boolean failOnError, final boolean ignore404)
    {
        this.failOnError = failOnError;
        this.ignore404 = ignore404;
    }

    @Override
    public Boolean convert(HttpClientResponse httpClientResponse, InputStream inputStream) throws IOException
    {
        final int responseCode = httpClientResponse.getStatusCode();
        switch (responseCode) {
            case 200:
            case 201:
            case 204:
                return Boolean.TRUE;

            case 404:
                if (ignore404) {
                    return Boolean.FALSE;
                }
                // FALL THROUGH
            default:
                if (failOnError) {
                    LOG.warn("Remote service responsed with %d code (cause: %s)", responseCode, httpClientResponse.getStatusText());
                    throw new HttpResponseException(httpClientResponse);
                }
                else {
                    return Boolean.FALSE;
                }
        }
    }
}
