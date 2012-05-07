package io.trumpet.httpclient;

import io.trumpet.httpclient.response.ContentResponseHandler;
import io.trumpet.httpclient.testsupport.GenericTestHandler;
import io.trumpet.httpclient.testsupport.LocalHttpService;
import io.trumpet.httpclient.testsupport.StringResponseConverter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.testing.lessio.AllowNetworkAccess;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;

import static org.junit.Assert.fail;

@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
public class TestAlwaysTrustServerSSLCert {
    private HttpClientResponseHandler<String> responseHandler =
        new ContentResponseHandler<String>(new StringResponseConverter());
    protected HttpClient httpClient = null;
    private LocalHttpService localHttpService;

    @Before
    public void setUp() {
        localHttpService = LocalHttpService.forSSLHandler(new GenericTestHandler());
        localHttpService.start();
    }

    @After
    public void tearDown() {
        Assert.assertNotNull(httpClient);
        httpClient.close();
        httpClient = null;
    }

    @Test
    public void testWithServerCertVerificationEnabled() throws IOException {

        final HttpClientDefaults defaults = getDefaults(true);

        httpClient = new HttpClient(defaults).start();

        try {
            final String uri = "https://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";
            httpClient.get(uri, responseHandler).perform();
            fail();
        } catch (SSLPeerUnverifiedException ignored) {
            // success
        }
    }

    @Test
    public void testWithServerCertVerificationDisabled() throws IOException {

        final HttpClientDefaults defaults = getDefaults(false);

        httpClient = new HttpClient(defaults).start();

        final String uri = "https://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";
        final String response = httpClient.get(uri, responseHandler).perform();
        Assert.assertNotNull(response);
    }

    private static HttpClientDefaults getDefaults(final boolean isServerCertVerification) {
        // no truststore and no fallback => default trust manager, which does not contain the comodo CA certs used
        // by localHttpService
        return new HttpClientDefaults() {
            @Override
            public String getSSLTruststore() {
                return null;
            }

            @Override
            public boolean isSSLTruststoreFallback() {
                return false;
            }

            @Override
            public boolean isSSLServerCertVerification() {
                return isServerCertVerification;
            }
        };
    }
}
