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

import java.io.IOException;
import java.io.InputStream;

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
public class TestCharset
{
    private GenericTestHandler testHandler = null;
    private LocalHttpService localHttpService = null;
    private HttpClient httpClient = null;

    @Before
    public void setup()
    {
        testHandler = new GenericTestHandler();
        localHttpService = LocalHttpService.forHandler(testHandler);
        localHttpService.start();

        httpClient = new HttpClient().start();
    }

    @After
    public void teardown()
    {
        localHttpService.stop();
        localHttpService = null;
        testHandler = null;

        httpClient.close();
        httpClient = null;
    }

    @Test
    public void testNoCharset() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

        testHandler.setContentType("text/plain");

        HttpClientResponseHandler<String> responseHandler = new ContentResponseHandler<String>(new StringResponseConverter() {
            @Override
            public String convert(final HttpClientResponse response, final InputStream inputStream) throws IOException
            {
                Assert.assertThat(response.getCharset(), is("ISO-8859-1")); // Jetty sets a default value here if none present.
                return super.convert(response, inputStream);
            }
        });

        httpClient.get(uri, responseHandler).perform();
    }


    @Test
    public void testCharsetHeader() throws IOException
    {
        final String testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);

        final String uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

        testHandler.setContentType("text/plain; charset=ISO-2022-JP");

        HttpClientResponseHandler<String> responseHandler = new ContentResponseHandler<String>(new StringResponseConverter() {
            @Override
            public String convert(final HttpClientResponse response, final InputStream inputStream) throws IOException
            {
                Assert.assertThat(response.getCharset(), is("ISO-2022-JP"));
                return super.convert(response, inputStream);
            }
        });

        httpClient.get(uri, responseHandler).perform();
    }
}
