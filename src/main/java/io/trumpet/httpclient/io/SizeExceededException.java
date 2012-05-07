package io.trumpet.httpclient.io;

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
