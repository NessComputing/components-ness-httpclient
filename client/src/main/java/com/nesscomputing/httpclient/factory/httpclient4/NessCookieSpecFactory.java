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
package com.nesscomputing.httpclient.factory.httpclient4;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.HttpParams;

/**
 * Provides a cookie spec policy that will unconditionally add all cookies to the
 * outgoing request. This is used for internal requests that need to proxy the
 * auth cookies forward.
 */
public class NessCookieSpecFactory implements CookieSpecFactory
{
    public static final String NESS_COOKIE_POLICY = "ness";

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

            registerAttribHandler(ClientCookie.DOMAIN_ATTR, new NessDomainHandler());

        }

        @Override
        public String toString()
        {
            return NESS_COOKIE_POLICY;
        }

    }

    public static class NessDomainHandler implements CookieAttributeHandler
    {
        @Override
        public void parse(final SetCookie cookie, final String value)
        throws MalformedCookieException
        {
            if (cookie == null) {
                throw new IllegalArgumentException("Cookie may not be null");
            }
            if (value == null) {
                throw new MalformedCookieException("Missing value for domain attribute");
            }
            if (value.trim().length() == 0) {
                throw new MalformedCookieException("Blank value for domain attribute");
            }
            cookie.setDomain(value);
        }

        @Override
        public void validate(final Cookie cookie, final CookieOrigin origin)
        throws MalformedCookieException
        {
            if (cookie == null) {
                throw new IllegalArgumentException("Cookie may not be null");
            }
            if (origin == null) {
                throw new IllegalArgumentException("Cookie origin may not be null");
            }

            // Everything else is allowed.
        }

        @Override
        public boolean match(final Cookie cookie, final CookieOrigin origin)
        {
            if (cookie == null) {
                throw new IllegalArgumentException("Cookie may not be null");
            }
            if (origin == null) {
                throw new IllegalArgumentException("Cookie origin may not be null");
            }

            // Everything matches.
            return true;
        }
    }
}
