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
package com.nesscomputing.httpclient.response;

import static java.lang.String.format;

import java.io.IOException;

import com.nesscomputing.httpclient.HttpClientResponse;

public class HttpResponseException extends IOException
{
    private static final long serialVersionUID = 1L;

    private final HttpClientResponse httpResponse;

    public HttpResponseException(final HttpClientResponse httpResponse)
    {
        this.httpResponse = httpResponse;
    }

    public int getStatusCode()
    {
        return httpResponse != null ? httpResponse.getStatusCode() : -1;
    }

    public HttpClientResponse getHttpResponse()
    {
        return httpResponse;
    }

    @Override
    public String toString()
    {
        if (httpResponse == null) {
            return "reason unknown";
        }
        else {
            return format("Status code: %d, Cause: %s", httpResponse.getStatusCode(), httpResponse.getStatusText());
        }
    }
}
