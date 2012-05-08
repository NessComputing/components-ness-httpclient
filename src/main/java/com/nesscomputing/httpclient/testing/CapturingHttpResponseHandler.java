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


import java.io.IOException;

import javax.annotation.concurrent.Immutable;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.HttpClientResponseHandler;

/**
 * Simple {@link HttpClientResponseHandler} which does nothing but expose the internal
 * {@link HttpClientResponse}.  Mostly useful for testing when you want to assert on
 * properties of the HTTP response directly rather than converting it into a domain object.
 */
@Immutable
public class CapturingHttpResponseHandler implements HttpClientResponseHandler<HttpClientResponse> {
    @Override
    public HttpClientResponse handle(HttpClientResponse response)
    throws IOException {
        return response;
    }
}
