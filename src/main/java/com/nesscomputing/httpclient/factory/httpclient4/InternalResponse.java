package com.nesscomputing.httpclient.factory.httpclient4;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import com.google.common.collect.Lists;

/**
 * Apache HttpClient4 implementation of {@link HttpClientResponse}.
 */
final class InternalResponse implements HttpClientResponse
{
    private static final Log LOG = Log.findLog();

    private final HttpRequestBase httpRequest;
    private final HttpResponse httpResponse;

    InternalResponse(final HttpRequestBase httpRequest, final HttpResponse httpResponse)
    {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    @Override
    public String getContentType()
    {
        if (httpResponse != null) {
            final HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                final Header contentType = entity.getContentType();
                return (contentType == null) ? null : contentType.getValue();
            }
        }
        return null;
    }

    @Override
    public Long getContentLength()
    {
        if (httpResponse != null) {
            final Header header = httpResponse.getFirstHeader("Content-Length");
            if (header != null) {
                final String contentLen = StringUtils.trimToEmpty(header.getValue());
                LOG.debug("Response content length header is '%s'", contentLen);
                try {
                    return Long.parseLong(contentLen);
                }
                catch (NumberFormatException nfe) {
                    LOG.warnDebug(nfe, "Could not parse '%s'", contentLen);
                }
            }
        }
        return null;
    }

    @Override
    public String getCharset()
    {
        if (httpResponse != null) {
            final HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                return EntityUtils.getContentCharSet(entity);
            }
        }
        return null;
    }

    @Override
    public String getHeader(final String name)
    {
        if (httpResponse != null) {
            final Header header = httpResponse.getFirstHeader(name);
            return (header == null) ? null : header.getValue();
        }
        return null;
    }

    @Override
    @Nonnull
    public List<String> getHeaders(final String name)
    {
        final List<String> values = new ArrayList<String>();
        if (httpResponse != null) {
            for(HeaderIterator it = httpResponse.headerIterator(name); it.hasNext(); ) {
                final Header header = it.nextHeader();
                values.add(header.getValue());
            }
        }
        return values;
    }

	@Override
    @Nonnull
	public Map<String,List<String>> getAllHeaders() {
        Map<String, List<String>> headerMap = new TreeMap<String,
            List<String>>(String.CASE_INSENSITIVE_ORDER);

        Header[] headers = httpResponse.getAllHeaders();

        for (Header header : headers) {
            String name = header.getName();
            String value = header.getValue();

            List<String> valuesForThisHeader = headerMap.get(name);

            if (valuesForThisHeader == null) {
                valuesForThisHeader = Lists.newLinkedList();
                headerMap.put(name, valuesForThisHeader);
            }
            valuesForThisHeader.add(value);
        }

        return headerMap;
    }

    @Override
    public InputStream getResponseBodyAsStream() throws IOException
    {
        if (httpResponse != null) {
            final HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                return httpEntity.getContent();
            }
        }
        return new NullInputStream(0);
    }

    @Override
    public int getStatusCode()
    {
        if (httpResponse != null) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            return (statusLine == null) ? 0 : statusLine.getStatusCode();
        }
        return 0;
    }

    @Override
    public String getStatusText()
    {
        if (httpResponse != null) {
            final StatusLine statusLine = httpResponse.getStatusLine();
            return (statusLine == null) ? "" : statusLine.getReasonPhrase();
        }
        return "";
    }

    @Override
    public URI getUri()
    {
        return (httpRequest == null) ? null : httpRequest.getURI();
    }

    @Override
    public boolean isRedirected()
    {
        final int statusCode = getStatusCode();
        return (statusCode >= 300) && (statusCode <= 399);
    }

    @Override
    public String toString() {
        return String.format("InternalResponse [getContentType()=%s, getContentLength()=%s," +
            		" getCharset()=%s, getAllHeaders()=%s, getStatusCode()=%s," +
            		" getStatusText()=%s, getUri()=%s, isRedirected()=%s]",
                getContentType(), getContentLength(), getCharset(),
                getAllHeaders(), getStatusCode(), getStatusText(),
                getUri(), isRedirected());
    }
}
