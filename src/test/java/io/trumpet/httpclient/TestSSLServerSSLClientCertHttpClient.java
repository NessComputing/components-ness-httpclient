package io.trumpet.httpclient;

import io.trumpet.httpclient.testsupport.LocalHttpService;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Before;

public class TestSSLServerSSLClientCertHttpClient extends AbstractTestHttpClient {
	@Before
	public void setup() {
		Assert.assertNull(localHttpService);
		Assert.assertNull(httpClient);

		localHttpService = LocalHttpService.forSSLClientSSLServerHandler(testHandler,
			"/test-pki/separate-ca-certs/ca/client/cacert.jks", "password",
			"/test-pki/separate-ca-certs/server/server-cert-and-key.p12",
			"password",
			"PKCS12");
		localHttpService.start();

		final HttpClientDefaults defaults = new HttpClientDefaults() {
			@Override
			public String getSSLTruststore() {
				return Resources.getResource(this.getClass(),
					"/test-pki/separate-ca-certs/ca/server/cacert.jks")
					.toString();
			}

			@Override
			public String getSSLTruststorePassword() {
				return "password";
			}

			@Override
			public String getSSLKeystore() {
				return Resources.getResource(this.getClass(),
					"/test-pki/separate-ca-certs/client/client-cert-and-key.p12").toString();
			}

			@Override
			public String getSSLKeystorePassword() {
				return "password";
			}

			@Override
			public String getSSLKeystoreType() {
				return "PKCS12";
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

