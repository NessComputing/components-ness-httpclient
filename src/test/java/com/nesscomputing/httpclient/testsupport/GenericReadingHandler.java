package com.nesscomputing.httpclient.testsupport;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.nesscomputing.httpclient.internal.HttpClientHeader;


public class GenericReadingHandler extends AbstractHandler
{
    private String content = "";
    private String contentType = "text/html";

    private List<HttpClientHeader> headers = new ArrayList<HttpClientHeader>();

    private String method = null;

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

    public String getMethod()
    {
        return method;
    }
}
