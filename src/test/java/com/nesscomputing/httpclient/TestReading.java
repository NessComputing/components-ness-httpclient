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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.testsupport.GenericReadingHandler;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
public class TestReading {
	private GenericReadingHandler testHandler = null;
	private LocalHttpService localHttpService = null;
	private HttpClient httpClient = null;
	private final HttpClientResponseHandler<String> responseHandler =
		new ContentResponseHandler<String>(new StringResponseConverter());
	private String uri;
	private String testString;

	@Before
	public void setup() {
		testHandler = new GenericReadingHandler();
		localHttpService = LocalHttpService.forHandler(testHandler);
		localHttpService.start();

		httpClient = new HttpClient().start();
		uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

		testString = "Ich bin zwei Oeltanks";
		testHandler.setContent(testString);
		testHandler.setContentType("text/plain");
	}

	@After
	public void teardown() {
		localHttpService.stop();
		localHttpService = null;
		testHandler = null;

		httpClient.close();
		httpClient = null;
	}

	@Test
	public void testGet() throws IOException {
		final String response = httpClient.get(uri, responseHandler).request().perform();

		assertThat(response, is(testString));
		assertThat(testHandler.getMethod(), is("GET"));
	}

	@Test
	public void testHead() throws IOException {
		final String response = httpClient.head(uri, responseHandler).request().perform();

		assertThat(response, is(""));
		assertThat(testHandler.getMethod(), is("HEAD"));
	}

	@Test
	public void testDelete() throws IOException {
		final String response = httpClient.delete(uri, responseHandler).request().perform();

		assertThat(response, is(testString));
		assertThat(testHandler.getMethod(), is("DELETE"));
	}

	@Test
	public void testOptions() throws IOException {
		final String response = httpClient.options(uri, responseHandler).request().perform();

		assertThat(response, is(testString));
		assertThat(testHandler.getMethod(), is("OPTIONS"));
	}
}

