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

import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.testsupport.GenericWritingContentHandler;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;


@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
public class TestWriting
{
    private GenericWritingContentHandler testHandler = null;
    private LocalHttpService localHttpService = null;
    private HttpClient httpClient = null;
    private final HttpClientResponseHandler<String> responseHandler = new ContentResponseHandler<String>(new StringResponseConverter());

    @Before
    public void setup()
    {
        testHandler = new GenericWritingContentHandler();
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
    public void testPost() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        final String postString = "This is the post string";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";


        final HttpClientRequest<String> request = httpClient.post(uri, responseHandler).setContent(postString).request();
        final String response = request.perform();

        Assert.assertThat(response, is(testString));

        Assert.assertThat(testHandler.getPostData(), is(postString));
        Assert.assertThat(testHandler.getMethod(), is("POST"));
    }

    @Test
    public void testEmptyBody() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";

        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";


        final HttpClientRequest<String> request = httpClient.post(uri, responseHandler).request();
        final String response = request.perform();

        Assert.assertThat(response, is(testString));

        Assert.assertThat(testHandler.getPostData(), is(""));
        Assert.assertThat(testHandler.getMethod(), is("POST"));
    }


    @Test
    public void testPut() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        final String postString = "This is the post string";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";


        final HttpClientRequest<String> request = httpClient.put(uri, responseHandler).setContent(postString).request();
        final String response = request.perform();

        Assert.assertThat(response, is(testString));

        Assert.assertThat(testHandler.getPostData(), is(postString));
        Assert.assertThat(testHandler.getMethod(), is("PUT"));
    }
}

