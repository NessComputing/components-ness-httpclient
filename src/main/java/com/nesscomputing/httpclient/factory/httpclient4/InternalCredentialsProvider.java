package com.nesscomputing.httpclient.factory.httpclient4;


import java.security.Principal;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

import com.nesscomputing.httpclient.HttpClientAuthProvider;

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
