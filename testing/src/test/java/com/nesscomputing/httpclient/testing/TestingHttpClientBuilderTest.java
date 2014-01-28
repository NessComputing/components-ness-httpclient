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
package com.nesscomputing.httpclient.testing;

import static com.nesscomputing.httpclient.internal.HttpClientMethod.GET;
import static com.nesscomputing.httpclient.internal.HttpClientMethod.PUT;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.internal.HttpClientBodySource;
import com.nesscomputing.httpclient.internal.HttpClientMethod;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * This pretty well doubles as an example use case of the {@link TestingHttpClientBuilder}.
 */
public class TestingHttpClientBuilderTest {
    private final CapturingHttpResponseHandler handler = new CapturingHttpResponseHandler();

    @Test(expected=IllegalStateException.class)
    public void testEmptyClient() throws Exception {
        TestingHttpClientBuilder builder = new TestingHttpClientBuilder();
        HttpClient httpClient = builder.build();

        httpClient.get("/", handler).perform();
    }

    @Test
    public void testSimpleResponse() throws Exception {
        TestingHttpClientBuilder builder = new TestingHttpClientBuilder();
        builder.on(GET).of("/whozamit").respondWith(Response.noContent());
        builder.on(GET).of("/foo").respondWith(Response.ok("bar"));
        builder.on(GET).of("/").respondWith(Response.status(Status.NOT_FOUND));
        HttpClient httpClient = builder.build();

        HttpClientResponse response = httpClient.get("/whozamit", handler).perform();
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatusCode());

        response = httpClient.get("/foo", handler).perform();
        assertEquals(Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals("OK", response.getStatusText());
        assertEquals(Long.valueOf(3), response.getContentLength());
        assertEquals(MediaType.TEXT_PLAIN, response.getContentType());
        assertEquals("bar", IOUtils.toString(response.getResponseBodyAsStream(), "UTF-8"));

        response = httpClient.get("/", handler).perform();
        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testJsonResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TestingHttpClientBuilder builder = new TestingHttpClientBuilder().withObjectMapper(mapper);
        Map<String, String> superImportantMap = Collections.singletonMap("foo", "bar");
        builder.on(GET).of("/json").respondWith(Response.ok(superImportantMap));
        HttpClient httpClient = builder.build();

        HttpClientResponse response = httpClient.get("/json", handler).perform();
        assertEquals(Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getContentType());
        Map<String, String> result = mapper.readValue(response.getResponseBodyAsStream(), new TypeReference<Map<String, String>>() {});
        assertEquals(superImportantMap, result);
    }

    @Test
    public void testBinaryData() throws Exception {
        byte[] data = DigestUtils.sha("There's no dark side of the moon, really.  Matter of fact it's all dark.");

        TestingHttpClientBuilder builder = new TestingHttpClientBuilder();
        builder.on(GET).of("/").respondWith(Response.ok(data));
        HttpClient httpClient = builder.build();

        HttpClientResponse response = httpClient.get("/", handler).perform();
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getContentType());
        assertArrayEquals(data, IOUtils.toByteArray(response.getResponseBodyAsStream()));
    }

    @Test
    public void testIOExcetionResponse() throws Exception {
        TestingHttpClientBuilder builder = new TestingHttpClientBuilder();
        builder.on(GET).of("/exception").respondWith(new IOException("danger"));
        HttpClient httpClient = builder.build();

        boolean thrown = false;
        try {
            httpClient.get("/exception", handler).perform();
            fail("should throw");
        } catch (IOException e) {
        	assertEquals("danger", e.getMessage());
        	thrown = true;
        }
        assertTrue("should throw", thrown);
    }

    @Test
    public void testCustomMatcherAndGenerator() throws Exception {
        TestingHttpClientBuilder builder = new TestingHttpClientBuilder();
        RequestMatcher matcher = new RequestMatcher() {
            @Override
            public boolean apply(HttpClientRequest<?> input) {
                try {
                    Integer.parseInt(input.getUri().getPath().substring(1));
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            }
        };
        ResponseGenerator<?> generator = new ResponseGenerator<Object>() {
            @Override
            public HttpClientResponse respondTo(HttpClientRequest<Object> request) {
                HttpClientResponse response = createMock(HttpClientResponse.class);
                expect(response.getStatusCode()).andReturn(
                    Integer.parseInt(request.getUri().getPath().substring(1))).anyTimes();
                replay(response);
                return response;
            }
        };
        builder.whenMatches(matcher).respondWith(generator);

        HttpClient httpClient = builder.build();
        HttpClientResponse response = httpClient.get("/563", handler).perform();
        assertEquals(563, response.getStatusCode());

        try {
            httpClient.get("/asdf", handler).perform();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void testPut() throws Exception {
        TestingHttpClientBuilder builder = new TestingHttpClientBuilder();
        builder.on(PUT).of("/sandwich").respondWith(new ResponseGenerator<Object>() {
            @Override
            public HttpClientResponse respondTo(HttpClientRequest<Object> request) throws IOException {
                final Response response;
                HttpClientBodySource body = request.getHttpBodySource();
                if (body != null && "sudo".equals(IOUtils.toString(body.getContent(), "UTF-8"))) {
                    response = Response.created(URI.create("ok://sandwich")).build();
                } else {
                    response = Response.status(Status.FORBIDDEN).build();
                }
                return new JaxRsResponseHttpResponseGenerator(null, response).respondTo(request);
            }
        });
        HttpClient httpClient = builder.build();

        HttpClientResponse response = httpClient.put("/sandwich", handler).perform();
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatusCode());

        response = httpClient.put("/sandwich", handler).setContent("sudo").perform();
        assertEquals(Status.CREATED.getStatusCode(), response.getStatusCode());
        assertEquals("ok://sandwich", response.getHeader("Location"));
    }

    @Test
    public void testGetHeaderAndGetHeaders() throws Exception {
        TestingHttpClientBuilder builder = new TestingHttpClientBuilder();
        builder.on(HttpClientMethod.GET).of("out").respondWith(
                Response.status(500)
                .header("Connection", "closed")
                .header("Content-Type", "application/json; charset=bogus")
                .header("Content-Language", "klingon")
                .header("MAGIC", "a")
                .header("MAGIC", "b")
                .header("MAGIC", "c")
                .entity("{\"a\":\"okay\"}"));
        HttpClient http = builder.build();
        HttpClientResponse res = http.get("out", handler).perform();
        String header = res.getHeader("bogus");
        assertEquals(null, header);
        List<String> headers = res.getHeaders("bogus");
        assertEquals(null, headers);
        header = res.getHeader("Content-Language");
        assertEquals("klingon", header);
        headers = res.getHeaders("Content-Language");
        assertEquals(Lists.newArrayList("klingon"), headers);
        header = res.getHeader("magic");
        assertEquals("a", header);
        headers = res.getHeaders("magic");
        assertEquals(Lists.newArrayList("a","b","c"), headers);
    }
}
