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


import java.io.IOException;
import java.io.InputStream;

import com.nesscomputing.httpclient.HttpClientResponse;


/**
 * Convert an Input Stream and content type into a result object.
 */
public interface ContentConverter<T>
{
    /**
     * Called when the ContentResponseHandler wants to convert an input stream
     * into a response object. Calling this method does *not* imply a 2xx response code,
     * this method will be called for all response codes if no error occurs while processing
     * the response.
     *
     * @param response The response object from the Http client.
     * @param inputStream The response body as stream.
     * @return The result object. Can be null.
     * @throws IOException
     */
    T convert(HttpClientResponse response, InputStream inputStream) throws IOException;

    /**
     * Called if an exception occured while trying to convert the response data stream
     * into a response object.
     *
     * @param response The response object from the Http client.
     * @param ex Exception triggering this call.
     * @return A response object or null.
     * @throws IOException
     */
    T handleError(HttpClientResponse response, IOException ex) throws IOException;
}
