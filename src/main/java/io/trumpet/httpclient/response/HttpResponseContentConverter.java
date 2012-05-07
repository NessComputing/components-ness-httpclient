package io.trumpet.httpclient.response;

import io.trumpet.httpclient.HttpClientResponse;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ContentConverter implementation that exposes the response directly.
 */
public class HttpResponseContentConverter implements ContentConverter<HttpResponse> {
    @Override
    public HttpResponse convert(HttpClientResponse response, InputStream inputStream) throws IOException {
        return new HttpResponse(response.getStatusCode(), IOUtils.toByteArray(inputStream), response.getCharset(), headersFor(response.getAllHeaders()));
    }

    private Multimap<String, String> headersFor(Map<String, List<String>> allHeaders) {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Entry<String, List<String>> e : allHeaders.entrySet()) {
            builder.putAll(e.getKey(), e.getValue());
        }

        return builder.build();
    }

    @Override
    public HttpResponse handleError(HttpClientResponse response, IOException ex) throws IOException {
        throw ex;
    }
}
