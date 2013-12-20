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

import java.io.IOException;

/**
 * A content handler. It parses the response from the server and generates the
 * content object.
 */
public interface HttpClientResponseHandler<T>
{
    /**
     * Process the {@link HttpClientResponse} object and generate an appropriate
     * response object.
     */
    T handle(HttpClientResponse response) throws IOException;
}
