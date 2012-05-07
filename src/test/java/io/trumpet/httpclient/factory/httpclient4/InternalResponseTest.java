package io.trumpet.httpclient.factory.httpclient4;

import io.trumpet.httpclient.HttpClientResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class InternalResponseTest {

    @Test
    public void testCaseInsensitiveHeaders() {
        HttpRequestBase req = new HttpGet();
        HttpResponse response = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        response.setHeaders(
            new Header[]{new BasicHeader("namE1", "val1"), new BasicHeader("Name1", "val2"),
                new BasicHeader("NAME1", "val3")});

        HttpClientResponse clientResponse = new InternalResponse(req, response);

        Map<String, List<String>> expectedAllHeaders = Maps.newHashMap();
        List<String> values = Lists.newArrayList("val1", "val2", "val3");

        expectedAllHeaders.put("namE1", values);

        assertEquals(values, clientResponse.getHeaders("name1"));
        assertEquals(values, clientResponse.getHeaders("naMe1"));

        assertEquals(expectedAllHeaders, clientResponse.getAllHeaders());
    }
}
