package io.trumpet.httpclient.testing;

import io.trumpet.httpclient.HttpClientRequest;
import io.trumpet.httpclient.HttpClientResponse;

import java.io.IOException;

/** Response generator which throws an exception instead of generating a
 * response. Useful for testing error handling code.
 */
@SuppressWarnings("rawtypes")
public class ExceptionResponseGenerator implements ResponseGenerator {
    private final IOException io;

    ExceptionResponseGenerator(IOException e) {
        this.io = e;
    }

    @Override
    public HttpClientResponse respondTo(HttpClientRequest request)
    throws IOException {
    	throw io;
    }
}
