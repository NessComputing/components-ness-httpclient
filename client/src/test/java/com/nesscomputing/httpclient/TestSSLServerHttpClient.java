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


import com.google.common.io.Resources;

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

