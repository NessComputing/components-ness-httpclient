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
package com.nesscomputing.httpclient;

import java.util.Locale;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Authentication provider implementation for various default cases.
 *
 */
public class HttpClientDefaultAuthProvider implements HttpClientAuthProvider
{
    private final String scheme;
    private final String host;
    private final int port;
    private final String realm;

    private final String user;
    private final String password;

    /**
     * Returns an {@link HttpClientAuthProvider} that will accept any remote host and presents
     * the login and password as authentication credential.
     * @param login Login to use.
     * @param password Password to use.
     */
    public static final HttpClientAuthProvider forUser(final String login, final String password)
    {
        return new HttpClientDefaultAuthProvider(null, null, -1, null, login, password);
    }

    /**
     * Returns an {@link HttpClientAuthProvider} that will accept a specific remote host and presents
     * the login and password as authentication credential.
     *
     *
     * @param host The remote host for which the credentials are valid.
     * @param port Port for the remote host.
     * @param login Login to use.
     * @param password Password to use.
     */
    public static final HttpClientAuthProvider forUserAndHost(final String host, final int port, final String login, final String password)
    {
        return new HttpClientDefaultAuthProvider(null, host, port, null, login, password);
    }

    /**
     * Creates an {@link HttpClientAuthProvider} that will accept a specific remote host and realm and presents
     * the login and password as authentication credential.
     *
     * @param scheme The scheme to use. Can be null, then any scheme is accepted.
     * @param host The remote host for which the credentials are valid. Can be null, then any host is accepted.
     * @param port Port for the remote host. Can be -1, then any port is accepted.
     * @param realm The login realm presented by the server. Can be null, then any realm is accepted.
     * @param login Login to use.
     * @param password Password to use.
     */
    public HttpClientDefaultAuthProvider(final String scheme, final String host, final int port, final String realm, final String login, final String password)
    {
        // HTTP Client internally lowercases host and realm. Make sure that we match up.
        this.host =  (host != null) ? host.toLowerCase(Locale.ENGLISH) : null;
        this.realm =  (realm != null) ? realm.toLowerCase(Locale.ENGLISH) : null;

        this.scheme = scheme;
        this.port = port;
        this.user = login;
        this.password = password;
    }

    @Override
    public boolean acceptRequest(final String authScheme, final String authHost, final int authPort, final String authRealm)
    {
        return ((scheme == null || scheme.equals(authScheme))
                && (host == null || host.equals(authHost))
                && (port == -1 || port == authPort)
                && (realm == null || realm.equals(authRealm)));
    }

    @Override
    public String getScheme()
    {
        return scheme;
    }

    @Override
    public String getHost()
    {
        return host;
    }

    @Override
    public int getPort()
    {
        return port;
    }

    @Override
    public String getRealm()
    {
        return realm;
    }

    @Override
    public String getUser()
    {
        return user;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (!(other instanceof HttpClientDefaultAuthProvider)) {
            return false;
        }
        HttpClientDefaultAuthProvider castOther = (HttpClientDefaultAuthProvider) other;
        return new EqualsBuilder().append(scheme, castOther.scheme).append(host, castOther.host).append(port, castOther.port).append(realm, castOther.realm).append(user, castOther.user).append(password, castOther.password).isEquals();
    }

    private transient int hashCode;


    @Override
    public int hashCode()
    {
        if (hashCode == 0) {
            hashCode = new HashCodeBuilder().append(scheme).append(host).append(port).append(realm).append(user).append(password).toHashCode();
        }
        return hashCode;
    }

    private transient String toString;


    @Override
    public String toString()
    {
        if (toString == null) {
            toString = new ToStringBuilder(this).append("scheme", scheme).append("host", host).append("port", port).append("realm", realm).append("user", user).append("password", password).toString();
        }
        return toString;
    }

}

