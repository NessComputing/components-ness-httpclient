package com.nesscomputing.httpclient;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientDefaults;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowNetworkAccess(endpoints={"*:443"})
public class TestFallbackManager {
	private HttpClientResponseHandler<String> responseHandler =
		new ContentResponseHandler<String>(new StringResponseConverter());
	protected HttpClient httpClient = null;

	@After
	public void tearDown() {
		Assert.assertNotNull(httpClient);
		httpClient.close();
		httpClient = null;
	}

	@Test
	public void testNoFallback() throws IOException {

		final HttpClientDefaults defaults = new HttpClientDefaults() {
			@Override
			public String getSSLTruststore() {
				return Resources.getResource(this.getClass(), "/test-httpclient-keystore.jks")
					.toString();
			}

			@Override
			public String getSSLTruststorePassword() {
				return "verysecret";
			}

			@Override
			public boolean isSSLTruststoreFallback() {
				return false;
			}
		};

		httpClient = new HttpClient(defaults).start();

		try {
			final String uri = "https://encrypted.google.com/";
			httpClient.get(uri, responseHandler).perform();
			fail();
		} catch (SSLPeerUnverifiedException ignored) {
			// success
		}
	}

	@Test
	public void testWithFallback() throws IOException {

		final HttpClientDefaults defaults = new HttpClientDefaults() {
			@Override
			public String getSSLTruststore() {
				return Resources.getResource(this.getClass(), "/test-httpclient-keystore.jks")
					.toString();
			}

			@Override
			public String getSSLTruststorePassword() {
				return "verysecret";
			}

			@Override
			public boolean isSSLTruststoreFallback() {
				return true;
			}
		};

		httpClient = new HttpClient(defaults).start();

		final String uri = "https://encrypted.google.com/";
		final String response = httpClient.get(uri, responseHandler).perform();
		Assert.assertNotNull(response);
	}
}



