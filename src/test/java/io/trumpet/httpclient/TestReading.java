package io.trumpet.httpclient;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.trumpet.httpclient.response.ContentResponseHandler;
import io.trumpet.httpclient.testsupport.GenericReadingHandler;
import io.trumpet.httpclient.testsupport.LocalHttpService;
import io.trumpet.httpclient.testsupport.StringResponseConverter;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

