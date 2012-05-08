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
import com.nesscomputing.httpclient.testsupport.GenericTestHandler;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
public abstract class AbstractTestHttpClient
{
    protected final HttpClientResponseHandler<String> responseHandler = new ContentResponseHandler<String>(new StringResponseConverter());

    protected GenericTestHandler testHandler = null;
    protected LocalHttpService localHttpService = null;
    protected HttpClient httpClient = null;

    @Before
    public void setupHandler()
    {
        testHandler = new GenericTestHandler();
    }

    @After
    public void teardown()
    {
        Assert.assertNotNull(localHttpService);
        Assert.assertNotNull(testHandler);
        Assert.assertNotNull(httpClient);

        localHttpService.stop();
        localHttpService = null;
        testHandler = null;

        httpClient.close();
        httpClient = null;
    }

    protected abstract HttpClientRequest<String> getRequest();

    @Test
    public void testSimple() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final HttpClientRequest<String> httpRequest = getRequest();
        final String response = httpRequest.perform();

        Assert.assertThat(response, is(testString));
    }

    @Test
    public void testReuse() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");

        final HttpClientRequest<String> httpRequest = getRequest();

        final String response = httpRequest.perform();

        Assert.assertThat(response, is(testString));

        final String response2 = httpRequest.perform();
        Assert.assertThat(response2, is(testString));
    }
}

