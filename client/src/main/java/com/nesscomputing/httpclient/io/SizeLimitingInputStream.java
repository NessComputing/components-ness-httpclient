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
package com.nesscomputing.httpclient.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Limits the maximum size of data read from the underlying stream.
 *
 * As with all other input streams, this class is not thread safe.
 */
public class SizeLimitingInputStream extends FilterInputStream
{
    private final int maxSize;
    private int count;
    private int mark = -1;

    /**
     * A filter input stream that can read up to a certain length and throws exception
     * after surpassing the limit.
     *
     * @param in
     * @param limit
     * @throws IOException
     */
    public SizeLimitingInputStream(final InputStream in, final int limit) throws IOException
    {
        super(in);
        this.maxSize = limit;
        // fail fast
        if (in.available() > limit) {
            throwException();
        }
    }

    private final void ensureLimit()
        throws SizeExceededException
    {
        if (count > maxSize) {
            throwException();
        }
    }

    private final void throwException()
        throws SizeExceededException
    {
        throw new SizeExceededException("SizeLimitInputStream: maximum size %d exceeded, actual = %d", maxSize, count);
    }

    @Override
    public int read()
        throws IOException
    {
        int r = in.read();
        if (r > 0) {
            count += r;
        }
        ensureLimit();
        return r;
    }

    @Override
    public int read(final byte b[])
        throws IOException
    {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(final byte b[], int off, int len)
        throws IOException
    {
        int r = in.read(b, off, len);
        if (r > 0) {
            count += r;
        }
        ensureLimit();
        return r;
    }

    @Override
    public long skip(final long n)
        throws IOException
    {
        long r = in.skip(n);
        if (r > 0) {
            count += (int) r;
        }
        ensureLimit();
        return r;
    }

    @Override
    public synchronized void mark(final int readlimit)
    {
        mark = count;
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset()
        throws IOException
    {
        if (mark > 0) {
            count = mark;
        }
        mark = -1;
        in.reset();
    }

    @Override
    public int available()
        throws IOException
    {
        return in.available();
    }

    @Override
    public void close()
        throws IOException
    {
        in.close();
    }
}
