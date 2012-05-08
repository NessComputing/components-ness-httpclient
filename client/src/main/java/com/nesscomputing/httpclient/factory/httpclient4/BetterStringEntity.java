package com.nesscomputing.httpclient.factory.httpclient4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.protocol.HTTP;

import com.google.common.base.Preconditions;

/**
 * A self contained, repeatable entity that obtains its content from
 * a {@link String}.
 */
public class BetterStringEntity extends AbstractHttpEntity implements Cloneable
{
    protected final byte[] content;

    /**
     * Creates a StringEntity with the specified content and charset
     *
     * @param string content to be used. Not {@code null}.
     * @param charset character set to be used. May be {@code null}, in which case the default is {@link HTTP#DEFAULT_CONTENT_CHARSET} i.e. "ISO-8859-1"
     *
     * @throws IllegalArgumentException if the string parameter is null
     */
    BetterStringEntity(final String string, Charset charset)
    {
        super();
        Preconditions.checkArgument(string != null, "Source string may not be null");

        final Charset charsetObj = ObjectUtils.firstNonNull(charset, Charsets.ISO_8859_1);
        this.content = string.getBytes(charsetObj);
        setContentType(HTTP.PLAIN_TEXT_TYPE + HTTP.CHARSET_PARAM + charsetObj.name());
    }

    public boolean isRepeatable()
    {
        return true;
    }

    public long getContentLength()
    {
        return this.content.length;
    }

    public InputStream getContent() throws IOException
    {
        return new ByteArrayInputStream(this.content);
    }

    public void writeTo(final OutputStream outstream) throws IOException
    {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        outstream.write(this.content);
        outstream.flush();
    }

    /**
     * Tells that this entity is not streaming.
     *
     * @return <code>false</code>
     */
    public boolean isStreaming()
    {
        return false;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

}
