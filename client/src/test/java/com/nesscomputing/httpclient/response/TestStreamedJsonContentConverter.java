package com.nesscomputing.httpclient.response;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.easymock.EasyMock;
import org.junit.Test;

import com.nesscomputing.callback.Callback;
import com.nesscomputing.callback.CallbackCollector;
import com.nesscomputing.callback.CallbackRefusedException;
import com.nesscomputing.httpclient.HttpClientResponse;

public class TestStreamedJsonContentConverter
{
    public static final String TEST_JSON = "{\"results\": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], \"success\":true}";

    private final ObjectMapper mapper = new ObjectMapper();

    private static InputStream inputStream()
    {
        return new ByteArrayInputStream(TEST_JSON.getBytes(Charsets.UTF_8));
    }
    private static HttpClientResponse response(int status)
    {
        HttpClientResponse response = EasyMock.createMock(HttpClientResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(status);
        EasyMock.replay(response);
        return response;
    }

    @Test
    public void testSuccess() throws Exception
    {
        CallbackCollector<Integer> callback = new CallbackCollector<>();
        new StreamedJsonContentConverter<>(mapper, callback, new TypeReference<Integer>() {}).convert(response(200), inputStream());

        assertEquals(ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), callback.getItems());
    }

    @Test
    public void testRefuse() throws Exception
    {
        final List<Integer> items = Lists.newArrayList();
        Callback<Integer> callback = new Callback<Integer>() {
            @Override
            public void call(Integer item) throws Exception
            {
                if (item >= 5) {
                    throw new CallbackRefusedException();
                }

                items.add(item);
            }
        };
        new StreamedJsonContentConverter<>(mapper, callback, new TypeReference<Integer>() {}).convert(response(200), inputStream());

        assertEquals(ImmutableList.of(1, 2, 3, 4), items);
    }
}
