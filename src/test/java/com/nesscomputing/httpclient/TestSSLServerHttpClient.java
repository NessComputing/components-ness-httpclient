package com.nesscomputing.httpclient;


import com.google.common.io.Resources;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientDefaults;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;

import org.junit.Assert;
import org.junit.Before;

public class TestSSLServerHttpClient extends AbstractTestHttpClient {
	@Before
	public void setup() {
		Assert.assertNull(localHttpService);
		Assert.assertNull(httpClient);

		localHttpService = LocalHttpService.forSSLHandler(testHandler);
		localHttpService.start();

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
		};

		httpClient = new HttpClient(defaults).start();
	}

	@Override
	protected HttpClientRequest<String> getRequest() {
		final String uri =
			"https://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";
		return httpClient.get(uri, responseHandler).request();
	}
}

