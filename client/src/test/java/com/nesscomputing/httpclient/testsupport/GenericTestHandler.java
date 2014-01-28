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
package com.nesscomputing.httpclient.testsupport;


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

import com.nesscomputing.httpclient.internal.HttpClientHeader;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


public class GenericTestHandler extends AbstractHandler
{
    private String content = "";
    private String contentType = "text/html";

    private final Map<String, List<HttpClientHeader>> reqHeaders = new HashMap<String, List<HttpClientHeader>>();
    private Cookie [] cookies = null;

    private String method = null;
    private String requestURI = null;
    private int status = HttpServletResponse.SC_OK;

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

        requestURI = request.getRequestURI();

        httpResponse.setContentType(contentType);
        httpResponse.setStatus(status);
        status = HttpServletResponse.SC_OK;

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

    /*
     * @param status The next response code to be returned by a request.
     * When the next status hasn't been set, a 200 will be returned.
     */
    public void setNextStatus(final int status)
    {
        this.status = status;
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

    public String getRequestURI()
    {
        return requestURI;
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
