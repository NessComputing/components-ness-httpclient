package io.trumpet.httpclient;

import io.trumpet.httpclient.testsupport.LocalHttpService;

import org.junit.Assert;
import org.junit.Before;


public class TestHttpClient extends AbstractTestHttpClient
{
    @Before
    public void setup()
    {
        Assert.assertNull(localHttpService);
        Assert.assertNull(httpClient);

        localHttpService =  LocalHttpService.forHandler(testHandler);
        localHttpService.start();

        httpClient = new HttpClient().start();
    }

    @Override
    protected HttpClientRequest<String> getRequest()
    {
        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";
        return httpClient.get(uri, responseHandler).request();
    }
}

