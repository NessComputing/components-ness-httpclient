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

import static java.lang.String.format;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

/**
 * A basic implementation of ContentConverter that returns a number.
 */
@Immutable
public class NumberContentConverter<T extends Number> extends AbstractErrorHandlingContentConverter<T>
{
    public static <N extends Number> ContentResponseHandler<N> getResponseHandler(final Class<N> numberClass, final boolean ignore404)
    {
        return ContentResponseHandler.forConverter(getConverter(numberClass, ignore404));
    }

    public static <N extends Number> ContentConverter<N> getConverter(final Class<N> numberClass, final boolean ignore404)
    {
        return new NumberContentConverter<N>(numberClass, ignore404);
    }

    private static final Log LOG = Log.findLog();

    private final boolean ignore404;
    private final Class<T> numberClass;
    private final Method valueOfMethod;
    private final T emptyValue;

    protected NumberContentConverter(final Class<T> numberClass, final boolean ignore404)
    {
        try {
            this.numberClass = numberClass;
            this.ignore404 = ignore404;
            this.valueOfMethod = numberClass.getMethod("valueOf", String.class);
            this.emptyValue = numberClass.cast(safeInvoke("0"));
        }
        catch (NoSuchMethodException nsme) {
            throw Throwables.propagate(nsme);
        }
    }

    @Override
    public T convert(HttpClientResponse httpClientResponse, InputStream inputStream) throws IOException
    {
        final int responseCode = httpClientResponse.getStatusCode();
        switch (responseCode) {
            case 200:
            case 201:
                final Charset charset = Charset.forName(Objects.firstNonNull(httpClientResponse.getCharset(), "UTF-8"));
                final InputStreamReader reader = new InputStreamReader(inputStream, charset);

                try {
                    final String data = CharStreams.toString(reader);
                    final T result = numberClass.cast(safeInvoke(data));
                    if (result == null) {
                        // 201 may or may not return a body. Try parsing the body, return the empty value if
                        // none is there (same as 204).
                        if (responseCode == 201) {
                            return emptyValue;
                        }
                        throw new IllegalArgumentException(format("Could not parse result '%s'", data));
                    }
                    return result;
                }
                finally {
                    Closeables.closeQuietly(reader);
                }

            case 204:
                return emptyValue;

            case 404:
                if (ignore404) {
                    return emptyValue;
                }

                // FALL THROUGH

            default:
                LOG.warn("Remote service responsed with %d code (cause: %s)", responseCode, httpClientResponse.getStatusText());
                throw new HttpResponseException(httpClientResponse);
        }
    }

    private Object safeInvoke(final String value)
    {
        try {
            return valueOfMethod.invoke(null, value);
        }
        catch (Exception e) {
            LOG.warnDebug(e, "while reading %s", value);
            return null;
        }
    }
}
