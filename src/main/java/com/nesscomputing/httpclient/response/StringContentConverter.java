package com.nesscomputing.httpclient.response;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

/**
 * A basic implementation of ContentConverter when you only want a string back.
 */
@Immutable
public class StringContentConverter extends AbstractErrorHandlingContentConverter<String>
{
    public static final ContentConverter<String> DEFAULT_CONVERTER = new StringContentConverter();
    public static final ContentConverter<String> DEFAULT_404OK_CONVERTER = new StringContentConverter(true);

    public static final ContentResponseHandler<String> DEFAULT_RESPONSE_HANDLER = ContentResponseHandler.forConverter(DEFAULT_CONVERTER);
    public static final ContentResponseHandler<String> DEFAULT_404OK_RESPONSE_HANDLER = ContentResponseHandler.forConverter(DEFAULT_404OK_CONVERTER);

    private static final Log LOG = Log.findLog();

    private final boolean ignore404;

    protected StringContentConverter()
    {
        this(false);
    }

    protected StringContentConverter(final boolean ignore404)
    {
        this.ignore404 = ignore404;
    }

    @Override
    public String convert(HttpClientResponse httpClientResponse, InputStream inputStream) throws IOException
    {
        final int responseCode = httpClientResponse.getStatusCode();
        switch (responseCode) {
            case 200:
            case 201:
                final Charset charset = Charset.forName(Objects.firstNonNull(httpClientResponse.getCharset(), "UTF-8"));
                final InputStreamReader reader = new InputStreamReader(inputStream, charset);

                try {
                    return CharStreams.toString(reader);
                }
                finally {
                    Closeables.closeQuietly(reader);
                }

            case 204:
                return "";

            case 404:
                if (ignore404) {
                    return "";
                }

                // FALL THROUGH

            default:
                LOG.warn("Remote service responsed with %d code (cause: %s)", responseCode, httpClientResponse.getStatusText());
                throw new HttpResponseException(httpClientResponse);
        }
    }
}
