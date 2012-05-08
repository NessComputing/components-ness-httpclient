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

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientDefaultAuthProvider;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.testsupport.GenericTestHandler;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
public class TestWrongCredentials
{
    public static final String LOGIN_USER = "testuser";
    public static final String LOGIN_PASSWORD = "testpass";
    public final String testString = "Ich bin zwei Oeltanks";

    protected GenericTestHandler testHandler = null;
    protected LocalHttpService localHttpService = null;
    protected HttpClient httpClient = null;
    protected String uri = null;

    protected final HttpClientResponseHandler<String> authResponseHandler = new ContentResponseHandler<String>(new StringResponseConverter(HttpServletResponse.SC_UNAUTHORIZED));

    @Before
    public void setup()
    {
        Assert.assertNull(localHttpService);
        Assert.assertNull(httpClient);

        testHandler = new GenericTestHandler();
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        localHttpService = LocalHttpService.forSecureHandler(testHandler, LOGIN_USER, LOGIN_PASSWORD);
        localHttpService.start();

        httpClient = new HttpClient().start();

        uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";
    }

    @After
    public void teardown()
    {
        Assert.assertNotNull(localHttpService);
        Assert.assertNotNull(testHandler);
        Assert.assertNotNull(httpClient);
        Assert.assertNotNull(uri);

        localHttpService.stop();
        localHttpService = null;
        testHandler = null;

        httpClient.close();
        httpClient = null;

        uri = null;
    }

    //
    // To reduce confusion: The actual Asserts for the tests below
    // are in the StringResponseConverter, they test that the
    // Response code is 401 (UNAUTHORIZED)

    @Test
    public void testWrongUser() throws IOException
    {
        httpClient.get(uri, authResponseHandler)
            .addBasicAuth("somedude", LOGIN_PASSWORD)
            .perform();
    }

    @Test
    public void testWrongPassword() throws IOException
    {
        httpClient.get(uri, authResponseHandler)
            .addBasicAuth("somedude", LOGIN_PASSWORD)
            .perform();
    }

    @Test
    public void testNoAuth() throws IOException
    {
        httpClient.get(uri, authResponseHandler)
            .perform();
    }

    @Test
    public void testGetItRight() throws IOException
    {
        final String response = httpClient.get(uri, new ContentResponseHandler<String>(new StringResponseConverter(HttpServletResponse.SC_OK)))
            .addAuth(new HttpClientDefaultAuthProvider("BASIC", localHttpService.getHost(), localHttpService.getPort(), "test", LOGIN_USER, LOGIN_PASSWORD))
            .perform();

        Assert.assertThat(response, is(testString));
    }


    @Test
    public void testWrongRealm() throws IOException
    {
        httpClient.get(uri, authResponseHandler)
            .addAuth(new HttpClientDefaultAuthProvider(null, localHttpService.getHost(), localHttpService.getPort(), "foo-realm", LOGIN_USER, LOGIN_PASSWORD))
            .perform();
    }

    @Test
    public void testWrongHost() throws IOException
    {
        httpClient.get(uri, authResponseHandler)
            .addAuth(HttpClientDefaultAuthProvider.forUserAndHost("www.cnn.com", localHttpService.getPort(), LOGIN_USER, LOGIN_PASSWORD))
            .perform();
    }

    @Test
    public void testWrongPort() throws IOException
    {
        httpClient.get(uri, authResponseHandler)
            .addAuth(HttpClientDefaultAuthProvider.forUserAndHost(localHttpService.getHost(), 22, LOGIN_USER, LOGIN_PASSWORD))
            .perform();
    }

    @Test
    public void testWrongScheme() throws IOException
    {
        httpClient.get(uri, authResponseHandler)
            .addAuth(new HttpClientDefaultAuthProvider("DIGEST", localHttpService.getHost(), localHttpService.getPort(), "test", LOGIN_USER, LOGIN_PASSWORD))
            .perform();
    }
}

