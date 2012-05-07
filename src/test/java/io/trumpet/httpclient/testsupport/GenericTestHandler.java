package io.trumpet.httpclient.testsupport;

import io.trumpet.httpclient.internal.HttpClientHeader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


public class GenericTestHandler extends AbstractHandler
{
    private String content = "";
    private String contentType = "text/html";

    private final Map<String, List<HttpClientHeader>> reqHeaders = new HashMap<String, List<HttpClientHeader>>();
    private Cookie [] cookies = null;

    private String method = null;

    private List<HttpClientHeader> headers = new ArrayList<HttpClientHeader>();

    @SuppressWarnings("unchecked")
    @Override
    public void handle(final String target,
            final Request request,
            final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse)
    throws IOException, ServletException
    {
        method = request.getMethod();

        httpResponse.setContentType(contentType);
        httpResponse.setStatus(HttpServletResponse.SC_OK);

        for (final HttpClientHeader header: headers) {
            httpResponse.addHeader(header.getName(), header.getValue());
        }

        for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String headerName = e.nextElement();
            List<HttpClientHeader> headers = reqHeaders.get(headerName);
            if (headers == null) {
                headers = new ArrayList<HttpClientHeader>();
                reqHeaders.put(headerName, headers);
            }
            for (Enumeration<String> v = request.getHeaders(headerName); v.hasMoreElements(); ) {
                String headerValue = v.nextElement();
                HttpClientHeader header = new HttpClientHeader(headerName, headerValue);
                headers.add(header);
            }
        }

        cookies = request.getCookies();

        request.setHandled(true);

        final PrintWriter writer = httpResponse.getWriter();
        writer.print(content);
        writer.flush();
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(final String content)
    {
        this.content = content;
    }

    public void addHeader(final String name, final String value)
    {
        headers.add(new HttpClientHeader(name, value));
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(final String contentType)
    {
        this.contentType = contentType;
    }

    public Map<String, List<HttpClientHeader>> getHeaders()
    {
        return reqHeaders;
    }

    public List<HttpClientHeader> getHeaders(final String name)
    {
        return reqHeaders.get(name);
    }

    public String getMethod()
    {
        return method;
    }

    public Cookie [] getCookies()
    {
        return cookies;
    }
}
