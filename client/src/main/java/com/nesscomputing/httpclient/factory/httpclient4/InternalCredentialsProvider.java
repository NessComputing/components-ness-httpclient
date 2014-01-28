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


import java.security.Principal;
import java.util.List;

import com.nesscomputing.httpclient.HttpClientAuthProvider;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

/**
 * An Apache Httpclient credentials provider that uses the internal structures of the tc-httpclient to provide
 * credentials.
 *
 */
public class InternalCredentialsProvider implements CredentialsProvider
{
    private final List<HttpClientAuthProvider> authProviders;

    InternalCredentialsProvider(final List<HttpClientAuthProvider> authProviders)
    {
        this.authProviders = authProviders;
    }

    @Override
    public void setCredentials(final AuthScope authscope, final Credentials credentials)
    {
        throw new UnsupportedOperationException("credentials can not be added to this provider!");
    }

    @Override
    public Credentials getCredentials(final AuthScope authScope)
    {
        for (final HttpClientAuthProvider authProvider : authProviders) {
            if (authProvider.acceptRequest(authScope.getScheme(), authScope.getHost(), authScope.getPort(), authScope.getRealm())) {
                return new Credentials() {

                    @Override
                    public Principal getUserPrincipal()
                    {
                        return new BasicUserPrincipal(authProvider.getUser());
                    }

                    @Override
                    public String getPassword()
                    {
                        return authProvider.getPassword();
                    }

                };
            }
        }
        return null;
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("credentials can not be removed from this provider!");
    }
}
