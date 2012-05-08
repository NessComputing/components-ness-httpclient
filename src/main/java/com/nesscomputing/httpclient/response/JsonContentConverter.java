package com.nesscomputing.httpclient.response;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

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
            // FALL THROUGH
            default:
                LOG.warn("Remote service responsed with %d code (cause: %s)", responseCode, httpClientResponse.getStatusText());
                throw new HttpResponseException(httpClientResponse);
        }
    }
}
