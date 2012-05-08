package com.nesscomputing.httpclient;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.nesscomputing.httpclient.internal.HttpClientBodySource;
import com.nesscomputing.httpclient.internal.HttpClientFactory;
import com.nesscomputing.httpclient.internal.HttpClientHeader;
import com.nesscomputing.httpclient.internal.HttpClientMethod;


/**
 * A request to a remote server. Composed step-by-step using a builder.
 */
public class HttpClientRequest<T>
{
    private final HttpClientFactory httpClientFactory;

    private final HttpClientMethod httpMethod;
    private final URI url;
    private final HttpClientResponseHandler<T> httpHandler;
    private List<HttpClientHeader> headers = Collections.emptyList();
    private List<Cookie> cookies = Collections.emptyList();
    private Map<String, Object> parameters = Collections.emptyMap();
    private String virtualHost = null;
    private int virtualPort = -1;
    private HttpClientBodySource httpBodySource = null;
    private Object content = null;
    private String contentType = null;
    private String contentEncoding = null;
    private List<HttpClientAuthProvider> authProviders = null;

    private HttpClientRequest(final HttpClientFactory httpClientFactory, final HttpClientMethod httpMethod, final URI url, final HttpClientResponseHandler<T> httpHandler)
    {
        this.httpClientFactory = httpClientFactory;

        this.httpMethod = httpMethod;
        this.url = url;
        this.httpHandler = httpHandler;
    }

    private void setHeaders(@Nonnull final List<HttpClientHeader> headers)
    {
        Preconditions.checkArgument(headers != null, "headers must not be null!");
        this.headers = Collections.unmodifiableList(headers);
    }

    private void setCookies(final List<Cookie> cookies)
    {
        Preconditions.checkArgument(cookies != null, "cookies must not be null!");
        this.cookies = Collections.unmodifiableList(cookies);
    }

    private void setParameters(final Map<String, Object> parameters)
    {
        Preconditions.checkArgument(parameters != null, "parameters must not be null!");
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    private void setVirtualHost(final String virtualHost, final int virtualPort)
    {
        this.virtualHost = virtualHost;
        this.virtualPort = virtualPort;
    }

    private void setAuthProviders(final List<HttpClientAuthProvider> authProviders)
    {
        Preconditions.checkArgument(authProviders != null, "authProviders must not be null!");
        this.authProviders = Collections.unmodifiableList(authProviders);
    }

    private void setContent(final Object content, final String contentType, final String contentEncoding)
    {
        this.content = content;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;

        if (content != null) {
            httpBodySource = httpClientFactory.getHttpBodySourceFor(content);
        }
        if (httpBodySource != null) {

            if (contentType != null) {
                httpBodySource.setContentType(contentType);
            }

            if (contentEncoding != null) {
                httpBodySource.setContentEncoding(contentEncoding);
            }
        }
    }

    public InputStream getContent() throws IOException
    {
        // Gee, thanks a million for an incredibly bad named method.
        return httpBodySource.getContent();
    }

    /**
     * @return the HTTP method for this request.
     */
    public HttpClientMethod getHttpMethod()
    {
        return httpMethod;
    }

    /**
     * @return the URI for this request.
     */
    public URI getUri()
    {
        return url;
    }

    /**
     * @return the Response handler object for this request.
     */
    public HttpClientResponseHandler<T> getHttpHandler()
    {
        return httpHandler;
    }

    /**
     * @return a list of headers that are sent with this request.
     */
    public List<HttpClientHeader> getHeaders()
    {
        return headers;
    }

    /**
     * @return a list of Cookies that are sent with this request.
     */
    public List<Cookie> getCookies()
    {
        return cookies;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    /**
     * @return the virtual host name for this request.
     */
    public String getVirtualHost()
    {
        return virtualHost;
    }

    /**
     * @return the virtual host port for this request.
     */
    public int getVirtualPort()
    {
        return virtualPort;
    }

    /**
     * @return the authentication providers for this request.
     */
    public List<HttpClientAuthProvider> getAuthProviders()
    {
        return authProviders;
    }

    /**
     * @return the source for the body content for POST and PUT requests.
     */
    public HttpClientBodySource getHttpBodySource()
    {
        return httpBodySource;
    }

    /**
     * Execute the HTTP request and return the result.
     */
    public T perform()
        throws IOException
    {
        return httpClientFactory.performRequest(this);
    }

    //
    // =========================================================
    //
    // Methods for recreating a builder from a request
    //
    // =========================================================
    //


    private Object getContentObject()
    {
        return content;
    }

    private String getContentType()
    {
        return contentType;
    }

    private String getContentEncoding()
    {
        return contentEncoding;
    }

    private HttpClientFactory getHttpClientFactory()
    {
        return httpClientFactory;
    }

    public static final class Builder<Type>
    {
        private final List<HttpClientHeader> headers = Lists.newArrayList();
        private final List<Cookie> cookies = Lists.newArrayList();
        private final Map<String, Object> parameters = Maps.newHashMap();

        private final HttpClientFactory httpClientFactory;
        private final HttpClientResponseHandler<Type> httpHandler;

        private URI url;
        private HttpClientMethod httpMethod;
        private String virtualHost;
        private int virtualPort;

        private Object content;
        private String contentType;
        private String contentEncoding;

        private final List<HttpClientAuthProvider> authProviders = Lists.newArrayList();

        public static <T> Builder<T> fromRequest(final HttpClientRequest<T> request)
        {
            return new Builder<T>(request);
        }

        <T> Builder(final HttpClientFactory httpClientFactory, final HttpClientMethod httpMethod, final URI url, final HttpClientResponseHandler<Type> httpHandler)
        {
            this.httpClientFactory = httpClientFactory;
            this.httpMethod = httpMethod;
            this.url = url;
            this.httpHandler = httpHandler;
        }

        <T> Builder(final HttpClientRequest<Type> request)
        {
            this (request.getHttpClientFactory(), request.getHttpMethod(), request.getUri(), request.getHttpHandler());

            this.headers.addAll(request.getHeaders());
            this.cookies.addAll(request.getCookies());
            this.parameters.putAll(request.getParameters());
            this.authProviders.addAll(request.getAuthProviders());

            this.virtualHost = request.getVirtualHost();
            this.virtualPort = request.getVirtualPort();
            this.content = request.getContentObject();
            this.contentType = request.getContentType();
            this.contentEncoding = request.getContentEncoding();
        }

        public Builder<Type> setUrl(final URI url)
        {
            Preconditions.checkArgument(url != null, "URI must not be null!");
            this.url = url;

            return this;
        }

        /**
         * Add a header to the request.
         * @param header the header name
         * @param value the header value
         */
        public Builder<Type> addHeader(final String header, final String value)
        {
            Preconditions.checkArgument(header != null, "Header name must not be null!");
            Preconditions.checkArgument(value != null, "Header value must not be null!");

            headers.add(new HttpClientHeader(header, value));
            return this;
        }

        /**
         * Add a header to the request and remove all other existing headers.
         * @param header the header name
         * @param value the header value
         */
        public Builder<Type> replaceHeader(final String header, final String value)
        {
            for (Iterator<HttpClientHeader> it = headers.iterator(); it.hasNext(); ) {
                final HttpClientHeader oldHeader = it.next();
                if (StringUtils.equals(header, oldHeader.getName())) {
                    it.remove();
                }
            }
            return addHeader(header, value);
        }

        /**
         * @param cookie cookie to add to the request
         */
        public Builder<Type> addCookie(@Nonnull final Cookie cookie)
        {
            Preconditions.checkArgument(cookie != null, "Cookie must not be null!");

            cookies.add(cookie);
            return this;
        }

        /**
         * @param cookie cookie to add to the request
         */
        public Builder<Type> replaceCookie(@Nonnull final Cookie cookie)
        {
            for (Iterator<Cookie> it = cookies.iterator(); it.hasNext(); ) {
                final Cookie oldCookie = it.next();
                if (StringUtils.equals(cookie.getName(), oldCookie.getName())) {
                    it.remove();
                }
            }

            return addCookie(cookie);
        }

        /**
         * Context parameters for the HTTP request.
         */
        public Builder<Type> setParameter(final String key, final Object value)
        {
            Preconditions.checkArgument(key != null, "key must not be null!");

            parameters.put(key, value);
            return this;
        }



        /**
         * Set the virtual host for this request.
         *
         * @param virtualHost the host
         * @param virtualPort the port. Can be -1 to use the default port.
         */
        public Builder<Type> setVirtualHost(final String virtualHost, final int virtualPort)
        {
            this.virtualHost = virtualHost;
            this.virtualPort = virtualPort;
            return this;
        }

        /**
         * Set the content type sent with this request if it is a POST or PUT request.
         */
        public Builder<Type> setContentType(@Nonnull final String contentType)
        {
            if (contentType == null) {
                throw new IllegalArgumentException("Content type can not be null!");
            }

            this.contentType = contentType;

            return this;
        }

        /**
         * Set the content encoding sent with this request if it is a POST or PUT request.
         */
        public Builder<Type> setContentEncoding(@Nonnull final String contentEncoding)
        {
            if (contentEncoding == null) {
                throw new IllegalArgumentException("Content encoding can not be null!");
            }

            this.contentEncoding = contentEncoding;

            return this;
        }

        public Builder<Type> setContent(Multimap<String,String> kvPairs, String encoding) throws UnsupportedEncodingException {
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            for (Map.Entry<String,String> entry : kvPairs.entries()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (!first) {
                    sb.append("&");
                } else {
                    first = false;
                }

                sb.append(URLEncoder.encode(key, encoding));
                sb.append("=");
                if (key != null) {
                    sb.append(URLEncoder.encode(value, encoding));
                }
            }

            setContentEncoding(encoding);
            setContent(sb.toString());
            return this;
        }

        /**
         * Create the content for a POST or PUT request from a String.
         */
        public Builder<Type> setContent(final String content)
        {
            this.content = content;
            return this;
        }

        /**
         * Create the content for a POST or PUT request from a byte array.
         */
        public Builder<Type> setContent(final byte [] content)
        {
            this.content = content;
            return this;
        }

        /**
         * Create the content for a POST or PUT request from an input stream.
         */
        public Builder<Type> setContent(final InputStream content)
        {
            this.content = content;
            return this;
        }

        /**
         * Add basic authentication information.
         * @param user Username to use when authentication is requested.
         * @param password Password to use when authentication is requested.
         */
        public Builder<Type> addBasicAuth(final String user, final String password)
        {
            return addAuth(HttpClientDefaultAuthProvider.forUser(user, password));
        }

        /**
         * @param authProvider a custom authentication provider to use when authentication is requested.
         */
        public Builder<Type> addAuth(final HttpClientAuthProvider authProvider)
        {
            authProviders.add(authProvider);
            return this;
        }

        /**
         * Create a HttpClientRequest from the builder. The object is disconnected from the builder and the builder can be reused.
         */
        public HttpClientRequest<Type> request()
        {
            final HttpClientRequest<Type> httpClientRequest = new HttpClientRequest<Type>(httpClientFactory, httpMethod, url, httpHandler);

            httpClientRequest.setHeaders(headers);
            httpClientRequest.setCookies(cookies);
            httpClientRequest.setParameters(parameters);
            httpClientRequest.setVirtualHost(virtualHost, virtualPort);
            httpClientRequest.setContent(content, contentType, contentEncoding);
            httpClientRequest.setAuthProviders(authProviders);

            return httpClientRequest;
        }

        /**
         * Builds a HttpClientRequest from the builder an executes it in one go.
         * @return The return value of the request.
         * @throws IOException
         */
        public Type perform() throws IOException
        {
            return request().perform();
        }
    }

    @Override
    public String toString() {
        return String.format("HttpClientRequest [%s %s]", httpMethod, url);
    }
}
