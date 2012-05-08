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
			public boolean useSSLTruststoreFallback() {
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
			public boolean useSSLTruststoreFallback() {
				return true;
			}
		};

		httpClient = new HttpClient(defaults).start();

		final String uri = "https://encrypted.google.com/";
		final String response = httpClient.get(uri, responseHandler).perform();
		Assert.assertNotNull(response);
	}
}



