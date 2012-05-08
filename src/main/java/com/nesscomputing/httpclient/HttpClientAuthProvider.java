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

/**
 * Provides authentication information to the Httpclient. Can be implemented by external classes and then
 * plugged into a request to allow flexible authentication.
 */
public interface HttpClientAuthProvider
{
    /**
     * Called by the HttpClient for checking authentication information. Should return true if the
     * provided authentication parameters are deemed sufficient by the implementation. HttpClient
     * will then use the getters to retrieve exact authentication information, login and password.
     *
     * @param authScheme The authentication scheme used. E.g. "BASIC" or "DIGEST".
     * @param authHost The host requesting authentication.
     * @param authPort Port for the requesting host.
     * @param authRealm The authentication realm presented by the host.
     * @return True If the implementation accepts the authentication.
     *
     */
    boolean acceptRequest(String authScheme, String authHost, int authPort, String authRealm);

    /**
     * Return the accepted scheme. Can be null, then any scheme is accepted.
     */
    String getScheme();

    /**
     * Return the accepted host. Can be null, then any host is accepted.
     */
    String getHost();

    /**
     * Return the accepted port. Can be -1, then any port is accepted.
     */
    int getPort();

    /**
     * Return the accepted realm. Can be null, then any realm is accepted.
     */
    String getRealm();

    /**
     * Return an user name to present to the remote host.
     */
    String getUser();

    /**
     * Return a password to present to the remote host.
     */
    String getPassword();
}
