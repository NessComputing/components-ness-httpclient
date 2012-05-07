package io.trumpet.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import io.trumpet.httpclient.HttpClientRequest.Builder;
import io.trumpet.httpclient.guice.HttpClientModule;
import io.trumpet.httpclient.response.ContentResponseHandler;
import io.trumpet.httpclient.testsupport.GenericReadingHandler;
import io.trumpet.httpclient.testsupport.LocalHttpService;
import io.trumpet.httpclient.testsupport.StringResponseConverter;

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
                install (new HttpClientModule());
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
