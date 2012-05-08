package com.nesscomputing.httpclient.factory.httpclient4;

import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.HttpParams;

/**
 * Provides a cookie spec policy that will unconditionally add all cookies to the
 * outgoing request. This is used for internal requests that need to proxy the
 * auth cookies forward.
 */
public class TrumpetCookieSpecFactory implements CookieSpecFactory
{
    public static final String TRUMPET_COOKIE_POLICY = "trumpet";

    @Override
    public CookieSpec newInstance(final HttpParams params)
    {
        return new TrumpetCookieSpec();
    }

    public static class TrumpetCookieSpec  extends BrowserCompatSpec
    {
        TrumpetCookieSpec()
        {
            super();
        }

        @Override
        public boolean match(final Cookie cookie, final CookieOrigin origin) {
            if (cookie == null) {
                throw new IllegalArgumentException("Cookie may not be null");
            }
            if (origin == null) {
                throw new IllegalArgumentException("Cookie origin may not be null");
            }

            // Don't reject any cookie. Anything goes.
            return true;
        }
    }
}
