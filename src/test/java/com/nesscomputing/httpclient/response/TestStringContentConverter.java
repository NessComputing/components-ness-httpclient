package com.nesscomputing.httpclient.response;


import org.junit.Assert;
import org.junit.Before;

import com.nesscomputing.httpclient.AbstractTestHttpClient;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.response.StringContentConverter;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;

public class TestStringContentConverter extends AbstractTestHttpClient {

    @Before
    public void setup() {
        Assert.assertNull(localHttpService);
        Assert.assertNull(httpClient);

        localHttpService = LocalHttpService.forHandler(testHandler);
        localHttpService.start();

        httpClient = new HttpClient().start();
    }

    @Override
    protected HttpClientRequest<String> getRequest() {
        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";
        return httpClient.get(uri, new ContentResponseHandler<String>(new StringContentConverter())).request();
    }
}
