package io.trumpet.httpclient.testsupport;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import io.trumpet.httpclient.HttpClientResponse;
import io.trumpet.httpclient.response.ContentConverter;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletResponse;

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
