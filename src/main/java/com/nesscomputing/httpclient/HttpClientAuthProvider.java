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
