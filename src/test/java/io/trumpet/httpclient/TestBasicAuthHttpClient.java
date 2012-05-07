package io.trumpet.httpclient;

import io.trumpet.httpclient.testsupport.LocalHttpService;

import org.junit.Assert;
import org.junit.Before;


public class TestBasicAuthHttpClient extends AbstractTestHttpClient
{
    public static final String LOGIN_USER = "testuser";
    public static final String LOGIN_PASSWORD = "testpass";

    @Before
    public void setup()
    {
        Assert.assertNull(localHttpService);
        Assert.assertNull(httpClient);

        localHttpService = LocalHttpService.forSecureHandler(testHandler, LOGIN_USER, LOGIN_PASSWORD);
        localHttpService.start();

        httpClient = new HttpClient().start();
    }

    @Override
    protected HttpClientRequest<String> getRequest()
    {
        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";
        return httpClient.get(uri, responseHandler)
            .addBasicAuth(LOGIN_USER, LOGIN_PASSWORD)
            .request();
    }
}

