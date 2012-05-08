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

import java.io.IOException;

/**
 * Exception getting thrown when the preset size of an
 * input or output stream has been exceeeded.
 *
 * @see IOException
 */
public class SizeExceededException extends IOException
{
    private static final long serialVersionUID = 1L;

    public SizeExceededException()
    {
    }

    public SizeExceededException(final String message,
                                 final Object... args)
    {
        super(String.format(message, args));
    }

    public SizeExceededException(final Throwable cause)
    {
        super(cause);
    }

    public SizeExceededException(final Throwable cause,
                                 final String message,
                                 final Object... args)
    {
        super(String.format(message, args), cause);
    }
}
