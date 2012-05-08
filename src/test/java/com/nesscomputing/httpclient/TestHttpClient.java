package com.nesscomputing.httpclient;


import org.junit.Assert;
import org.junit.Before;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;


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

