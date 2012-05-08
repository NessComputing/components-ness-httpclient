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
public class NessCookieSpecFactory implements CookieSpecFactory
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
