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

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.logging.Log;

import java.io.IOException;

/**
 * Base class to get rid of the repetitive "throw the exception out" method.
 */
public abstract class AbstractErrorHandlingContentConverter<T> implements ContentConverter<T>
{
    private static final Log LOG = Log.findLog();

    @Override
    public T handleError(final HttpClientResponse httpClientResponse, final IOException ex)
        throws IOException
    {
        if (httpClientResponse != null) {
            LOG.trace("Failure cause is: %s", httpClientResponse.getStatusText());
        }
        throw ex;
    }
}
