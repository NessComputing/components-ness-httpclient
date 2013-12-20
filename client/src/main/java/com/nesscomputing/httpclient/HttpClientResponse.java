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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

/** Response from the remote server. */
public interface HttpClientResponse {
    /**
     * Returns the status code for the request.
     *
     * @return the status code, use the related {@link HttpServletRequest} constants
     */
    int getStatusCode();

    /**
     * Returns the status text for the request.
     *
     * @return the status text
     */
    String getStatusText();

    /**
     * Returns an input stream for the response body.
     *
     * @return the input stream
     * @throws IOException on error
     */
    InputStream getResponseBodyAsStream() throws IOException;

    /** @return the URI of the request. */
    URI getUri();

    /** @return Content type of the response. */
    String getContentType();

    /**
     * @return Length of the content, if present. Can be null (not present), -1 (chunked) or 0 (no
     *         content).
     */
    @CheckForNull
    Long getContentLength();

    /** @return Content charset if present in the header. Can be null. */
    @CheckForNull
    String getCharset();

    /**
     * @param name the header name
     * @return The named header from the response. Response can be null.
     */
    @CheckForNull
    String getHeader(String name);

    /**
     * @param name the header name
     * @return all values for the given header. Response can be null.
     */
    @CheckForNull
    List<String> getHeaders(String name);

    /** @return Map of header name -> list of values for each header name */
    @Nonnull
    Map<String, List<String>> getAllHeaders();

    /** @return true if the response redirects to another object. */
    boolean isRedirected();
}
