package io.trumpet.httpclient.response;

import io.trumpet.httpclient.AbstractTestHttpClient;
import io.trumpet.httpclient.HttpClient;
import io.trumpet.httpclient.HttpClientRequest;
import io.trumpet.httpclient.testsupport.LocalHttpService;

import org.junit.Assert;
import org.junit.Before;

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
