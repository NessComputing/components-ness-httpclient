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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

public class JsonContentConverter<T> extends AbstractErrorHandlingContentConverter<T>
{
    private static final Log LOG = Log.findLog();

    private final TypeReference<T> typeReference;
    private final ObjectMapper objectMapper;
    private final boolean ignore404;

    public static <CC> ContentResponseHandler<CC> getResponseHandler(final TypeReference<CC> typeReference, final ObjectMapper objectMapper)
    {
        return ContentResponseHandler.forConverter(getConverter(typeReference, objectMapper));
    }

    public static <Type> ContentConverter<Type> getConverter(final TypeReference<Type> typeReference, final ObjectMapper objectMapper)
    {
        return new JsonContentConverter<Type>(typeReference, objectMapper);
    }

    public static <CC> ContentResponseHandler<CC> getResponseHandler(final TypeReference<CC> typeReference, final ObjectMapper objectMapper, final boolean ignore404)
    {
        return ContentResponseHandler.forConverter(getConverter(typeReference, objectMapper, ignore404));
    }

    public static <Type> ContentConverter<Type> getConverter(final TypeReference<Type> typeReference, final ObjectMapper objectMapper, final boolean ignore404)
    {
        return new JsonContentConverter<Type>(typeReference, objectMapper, ignore404);
    }

    public JsonContentConverter(final TypeReference<T> typeReference,
                                final ObjectMapper objectMapper)
    {
        this(typeReference, objectMapper, false);
    }

    public JsonContentConverter(final TypeReference<T> typeReference,
                                final ObjectMapper objectMapper,
                                final boolean ignore404)
    {
        this.typeReference = typeReference;
        this.objectMapper = objectMapper;
        this.ignore404 = ignore404;
    }

    @Override
    public T convert(final HttpClientResponse httpClientResponse,
                     final InputStream inputStream)
    throws IOException
    {
        final int responseCode = httpClientResponse.getStatusCode();
        switch (responseCode) {
            case 200:
                return objectMapper.<T>readValue(inputStream, typeReference);

            case 204:
                return null; // Return null for "CREATED" response code.

                // 201 may or may not contain a result object
            case 201:
                try {
                    return objectMapper.<T>readValue(inputStream, typeReference);
                }
                catch (JsonParseException jpe) {
                    LOG.trace(jpe, "while reading response");
                    return null;
                }

            case 404:
                if (ignore404) {
                    return null;
                }
                throw throwHttpResponseException(httpClientResponse);

            default:
                throw throwHttpResponseException(httpClientResponse);
        }
    }
}
