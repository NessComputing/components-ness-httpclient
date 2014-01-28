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

import javax.net.ssl.SSLException;

import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.testsupport.GenericTestHandler;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitei.testing.lessio.AllowNetworkAccess;

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
        } catch (SSLException e) {
            // ignore
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
            public boolean useSSLTruststoreFallback() {
                return false;
            }

            @Override
            public boolean useSSLServerCertVerification() {
                return isServerCertVerification;
            }
        };
    }
}
