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
package com.nesscomputing.httpclient.testsupport;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletResponse;

import com.nesscomputing.httpclient.HttpClientResponse;
import com.nesscomputing.httpclient.response.ContentConverter;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;


@Immutable
public class StringResponseConverter implements ContentConverter<String>
{
    private final int responseCode;


    public StringResponseConverter()
    {
        this(HttpServletResponse.SC_OK);
    }

    public StringResponseConverter(final int responseCode)
    {
        this.responseCode = responseCode;
    }

    @Override
    public String convert(final HttpClientResponse response, final InputStream inputStream)
        throws IOException
    {
        Assert.assertThat(response.getStatusCode(), is(equalTo(responseCode)));
        return IOUtils.toString(inputStream);
    }

    @Override
    public String handleError(HttpClientResponse response, IOException ex) throws IOException
    {
        throw new IllegalStateException(ex);
    }
}
