package io.trumpet.httpclient.response;

import java.io.UnsupportedEncodingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/** Simple bean for use in HttpResponseContentConverter. */
@Immutable
public class HttpResponse {
    private final byte[] body;
    @Nullable
    private final String charset;
    private final int statusCode;
    private final Multimap<String, String> headers;

    HttpResponse(int statusCode, @Nonnull byte[] body, @Nullable String charset, @Nonnull Multimap<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.charset = charset;
        this.headers = ImmutableMultimap.copyOf(headers);
    }

    public Multimap<String, String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** @return the body */
    @Nonnull
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_REP")
    public byte[] getBody() {
        return body;
    }

    /**
     * @return the body interpreted as a string. If a charset was returned in the http response, it will be used. If
     *         not, the body will be interpreted as UTF-8.
     * @throws UnsupportedEncodingException if the encoding is unsupported
     */
    @Nonnull
    public String getBodyAsString() throws UnsupportedEncodingException {
        if (charset == null) {
            return new String(body, Charsets.UTF_8);
        }
        return new String(body, charset);
    }
}
