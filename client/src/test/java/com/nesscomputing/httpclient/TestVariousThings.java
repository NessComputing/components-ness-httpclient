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
package com.nesscomputing.httpclient;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;


import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.internal.HttpClientHeader;
import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.testsupport.GenericTestHandler;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;


@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
public class TestVariousThings
{
    private GenericTestHandler testHandler = null;
    private LocalHttpService localHttpService = null;
    private HttpClient httpClient = null;
    private final HttpClientResponseHandler<String> responseHandler = new ContentResponseHandler<String>(new StringResponseConverter());

    @Before
    public void setup()
    {
        testHandler = new GenericTestHandler();
        localHttpService =  LocalHttpService.forHandler(testHandler);
        localHttpService.start();

        httpClient = new HttpClient().start();
    }

    @After
    public void teardown()
    {
        localHttpService.stop();
        localHttpService = null;
        testHandler = null;

        httpClient.close();
        httpClient = null;
    }

    @Test
    public void testHeaders() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";


        final HttpClientRequest<String> httpRequest = httpClient.get(uri, responseHandler).addHeader("Header1", "Value1").addHeader("header2", "value2").request();
        final String response = httpRequest.perform();

        Assert.assertThat(response, is(testString));
        Assert.assertThat(testHandler.getMethod(), is("GET"));

        List<HttpClientHeader> header1s = testHandler.getHeaders("Header1");
        Assert.assertThat(header1s, is(notNullValue()));
        Assert.assertThat(header1s.size(), is(1));

        HttpClientHeader header1 = header1s.get(0);
        Assert.assertThat(header1.getName(), is("Header1"));
        Assert.assertThat(header1.getValue(), is("Value1"));

        List<HttpClientHeader> header2s = testHandler.getHeaders("header2");
        Assert.assertThat(header2s, is(notNullValue()));
        Assert.assertThat(header2s.size(), is(1));

        HttpClientHeader header2 = header2s.get(0);
        Assert.assertThat(header2.getName(), is("header2"));
        Assert.assertThat(header2.getValue(), is("value2"));
    }

    @Test
    public void testMultiHeaders() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";


        final HttpClientRequest<String> httpRequest = httpClient.get(uri, responseHandler).addHeader("Header1", "Value1").addHeader("Header1", "Value2").request();
        final String response = httpRequest.perform();

        Assert.assertThat(response, is(testString));
        Assert.assertThat(testHandler.getMethod(), is("GET"));

        List<HttpClientHeader> header1s = testHandler.getHeaders("Header1");
        Assert.assertThat(header1s, is(notNullValue()));
        Assert.assertThat(header1s.size(), is(2));

        HttpClientHeader header1 = header1s.get(0);
        HttpClientHeader header2 = header1s.get(1);

        Assert.assertThat(header1, is(notNullValue()));
        Assert.assertThat(header2, is(notNullValue()));

        if (header1.getValue().contains("2")) {
            header1 = header1s.get(1);
            header2 = header1s.get(0);
        }

        Assert.assertThat(header1.getName(), is("Header1"));
        Assert.assertThat(header1.getValue(), is("Value1"));

        Assert.assertThat(header2.getName(), is("Header1"));
        Assert.assertThat(header2.getValue(), is("Value2"));
    }

    @Test
    public void testCookie() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

        Cookie cookie = new Cookie("cookie", "chocolate");
        cookie.setDomain(localHttpService.getHost());
        cookie.setPath("/");
        cookie.setMaxAge(3600);

        final HttpClientRequest<String> httpRequest = httpClient.get(uri, responseHandler).addCookie(cookie).request();
        final String response = httpRequest.perform();

        Assert.assertThat(response, is(testString));
        Assert.assertThat(testHandler.getMethod(), is("GET"));

        Cookie [] cookies = testHandler.getCookies();
        Assert.assertThat(cookies, is(notNullValue()));
        Assert.assertThat(cookies.length, is(1));

        Assert.assertThat(cookies[0].getName(), equalTo(cookie.getName()));
        Assert.assertThat(cookies[0].getValue(), equalTo(cookie.getValue()));
    }

    @Test(expected=IOException.class)
    public void testResponseExplodes() throws IOException
    {
        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

        final HttpClientRequest<String> httpRequest = httpClient.get(uri, new HttpClientResponseHandler<String>() {

            @Override
            public String handle(HttpClientResponse response) throws IOException
            {
                throw new IOException();
            }
        }).request();

        httpRequest.perform();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResponseExplodes2() throws IOException
    {
        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

        final HttpClientRequest<String> httpRequest = httpClient.get(uri, new HttpClientResponseHandler<String>() {

            @Override
            public String handle(HttpClientResponse response) throws IOException
            {
                throw new IllegalArgumentException();
            }
        }).request();

        httpRequest.perform();
    }

    @Test(expected=IllegalStateException.class)
    public void testClosedFactory() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

        httpClient.close();

        final HttpClientRequest<String> httpRequest = httpClient.get(uri, responseHandler).request();
        httpRequest.perform();
    }

    @Test
    public void testVirtualHost() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";


        final HttpClientRequest<String> httpRequest = httpClient.get(uri, responseHandler).setVirtualHost("www.nesscomputing.com", 8080).request();
        final String response = httpRequest.perform();

        Assert.assertThat(response, is(testString));
        Assert.assertThat(testHandler.getMethod(), is("GET"));

        List<HttpClientHeader> hostHeader = testHandler.getHeaders("Host");
        Assert.assertThat(hostHeader, is(notNullValue()));
        Assert.assertThat(hostHeader.size(), is(1));

        HttpClientHeader header = hostHeader.get(0);
        Assert.assertThat(header.getName(), is("Host"));
        Assert.assertThat(header.getValue(), is("www.nesscomputing.com:8080"));
    }
}

