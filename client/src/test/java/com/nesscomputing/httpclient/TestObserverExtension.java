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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Stage;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientObserver;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.HttpClientResponseHandler;
import com.nesscomputing.httpclient.HttpClientRequest.Builder;
import com.nesscomputing.httpclient.guice.HttpClientModule;
import com.nesscomputing.httpclient.response.ContentResponseHandler;
import com.nesscomputing.httpclient.testsupport.GenericReadingHandler;
import com.nesscomputing.httpclient.testsupport.LocalHttpService;
import com.nesscomputing.httpclient.testsupport.StringResponseConverter;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.logging.Log;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;

@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
public class TestObserverExtension {
    private static final Log LOG = Log.findLog();
    private LocalHttpService localHttpService = null;
    @Inject
    @Named("test")
    private HttpClient httpClient = null;
    @Inject
    private Lifecycle lifecycle;
    private final HttpClientResponseHandler<String> responseHandler =
        new ContentResponseHandler<String>(new StringResponseConverter());
    private String uri;
    private String testString;

    @Before
    public void setup() {
        GenericReadingHandler testHandler = new MirroredHeaderHandler();
        localHttpService = LocalHttpService.forHandler(testHandler);
        localHttpService.start();

        Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();
                binder().disableCircularProxies();
                install (ConfigModule.forTesting());
                install (new LifecycleModule());
                install (new HttpClientModule("test"));
                HttpClientModule.bindNewObserver(binder()).toInstance(new MyObserver());
            }
        }).injectMembers(this);

        lifecycle.executeTo(LifecycleStage.START_STAGE);
        uri = "http://" + localHttpService.getHost() + ":" + localHttpService.getPort() + "/data";

        testString = "Ich bin zwei Oeltanks";
        testHandler.setContent(testString);
        testHandler.setContentType("text/plain");
    }

    @After
    public void teardown() {
        localHttpService.stop();
        localHttpService = null;

        lifecycle.execute(LifecycleStage.STOP_STAGE);
        httpClient = null;
    }

    private volatile boolean checkedHeader = false;

    @Test
    public void testInsertHeader() throws IOException {
        httpClient.get(uri, responseHandler).request().perform();

        assertTrue(checkedHeader);
    }

    private class MyObserver extends HttpClientObserver {
        @Override
        public <RequestType> HttpClientRequest<RequestType> onRequestSubmitted(final HttpClientRequest<RequestType> request)
        {
            final Builder<RequestType> builder = HttpClientRequest.Builder.fromRequest(request);
            LOG.info("Added header");
            builder.addHeader("X-Observed", "true");
            return builder.request();
        }

        @Override
        public HttpClientResponse onResponseReceived(HttpClientResponse response) {
            LOG.info("Checking header");
            assertEquals("true", response.getHeader("X-Observed"));
            checkedHeader = true;
            return response;
        }
    }

    private static class MirroredHeaderHandler extends GenericReadingHandler {
        @Override
        public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
                throws IOException, ServletException {
            httpResponse.setContentType("text/plain");
            httpResponse.setStatus(HttpServletResponse.SC_OK);

            final String header = httpRequest.getHeader("X-Observed");
            Assert.assertNotNull(header);
            httpResponse.addHeader("X-Observed", header);

            request.setHandled(true);

            final PrintWriter writer = httpResponse.getWriter();
            writer.print("blah");
            writer.flush();
        }
    }
}
