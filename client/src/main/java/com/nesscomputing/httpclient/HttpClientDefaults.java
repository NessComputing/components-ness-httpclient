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

import java.util.concurrent.TimeUnit;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;
import org.skife.config.TimeSpan;

/**
 * Default configuration bean for the HTTP client. Can be configured using config-magic.
 */
public class HttpClientDefaults
{
    /**
     * Default is 200.
     *
     * @return The maximum number of connections allowed.
     */
    @Config({"ness.httpclient.${httpclient_name}.total-connections-max", "ness.httpclient.total-connections-max"})
    @Default("200")
    public int getTotalConnectionsMax()
    {
        return 200;
    }

    /**
     * Default is 20.
     *
     * @return The number of connections allowed to a single host/port combination.
     */
    @Config({"ness.httpclient.${httpclient_name}.perhost-connections-max", "ness.httpclient.perhost-connections-max"})
    @Default("20")
    public int getPerHostConnectionsMax()
    {
        return 20;
    }

    /**
     * Default is 60000 (60 seconds).
     *
     * @return Timeout connecting to a remote host in milliseconds.
     */
    @Config({"ness.httpclient.${httpclient_name}.connection-timeout", "ness.httpclient.connection-timeout"})
    @Default("60s")
    public TimeSpan getConnectionTimeout()
    {
        return new TimeSpan(60, TimeUnit.SECONDS);
    }

    /**
     * Default is an outrageously long 60s to match the existing outrageously long default request timeout
     *
     * @return  Socket timeout
     */
    @Config({"ness.httpclient.${httpclient_name}.socket-timeout", "ness.httpclient.socket-timeout"})
    @Default("60s")
    public TimeSpan getSocketTimeout()
    {
        return new TimeSpan(60, TimeUnit.SECONDS);
    }

    /**
     * Default is 15000 (15 seconds).
     *
     * @return Timeout for an established connection in keep-alive until it is closed. If 0,
     *         connections are closed immediately.
     */
    @Config({"ness.httpclient.${httpclient_name}.idle-timeout", "ness.httpclient.idle-timeout"})
    @Default("15s")
    public TimeSpan getIdleTimeout()
    {
        return new TimeSpan(15, TimeUnit.SECONDS);
    }

    /**
     * Default is 60000 (60 seconds).
     *
     * @return Timeout for a request sent until a response must arrive.
     */
    @Config({"ness.httpclient.${httpclient_name}.request-timeout", "ness.httpclient.request-timeout"})
    @Default("60s")
    public TimeSpan getRequestTimeout()
    {
        return new TimeSpan(60, TimeUnit.SECONDS);
    }

    /**
     * Default is true (follow redirects).
     *
     * @return true if redirects should be followed automatically.
     */
    @Config({"ness.httpclient.${httpclient_name}.follow-redirects", "ness.httpclient.follow-redirects"})
    @Default("true")
    public boolean isFollowRedirects()
    {
        return true;
    }

    /**
     * Default is 5.
     *
     * @return Maximum number of redirects until an error is returned.
     */
    @Config({"ness.httpclient.${httpclient_name}.max-redirects", "ness.httpclient.max-redirects"})
    @Default("5")
    public int getMaxRedirects()
    {
        return 5;
    }

    /**
     * Default is 'Ness HTTP Client'.
     *
     * @return Name of the user agent, used to identify to remote sites.
     */
    @Config({"ness.httpclient.${httpclient_name}.user-agent", "ness.httpclient.user-agent"})
    @Default("Ness HTTP Client")
    public String getUserAgent()
    {
        return "Ness HTTP Client";
    }

    /**
     * Default is 3.
     *
     * @return Maximum number of retries for a request.
     */
    @Config({"ness.httpclient.${httpclient_name}.retries", "ness.httpclient.retries"})
    @Default("3")
    public int getRetries()
    {
        return 3;
    }

    /**
     * This would be used to verify server certs that aren't signed by a standard CA.
     *
     * You must specify a password if you specify a truststore.
     *
     * @return Location of the SSL truststore.
     */
    @Config({"ness.httpclient.${httpclient_name}.ssl.truststore", "ness.httpclient.ssl.truststore"})
    @Default("classpath:/default-truststore.jks")
    public String getSSLTruststore()
    {
        return "classpath:/default-truststore.jks";
    }

    /**
     * You must specify a password if you specify a truststore. If either the truststore or truststore password is
     * null, the default truststore will be used.
     *
     * @return Password for the SSL truststore.
     */
    @Config({"ness.httpclient.${httpclient_name}.ssl.truststore.password", "ness.httpclient.ssl.truststore.password"})
    @Default("changeit")
    public String getSSLTruststorePassword()
    {
        return "changeit";
    }

    /**
     * This only applies if there is a custom truststore.
     *
     * @return true if the client should fall back to default truststore if the custom truststore can
     *         not validate a request.
     */
    @Config({"ness.httpclient.${httpclient_name}.ssl.truststore.fallback", "ness.httpclient.ssl.truststore.fallback"})
    @Default("true")
    public boolean useSSLTruststoreFallback()
    {
        return true;
    }

    /**
     * Can be "JKS" or "PKCS12". Note that Java can't read PKCS12 files that don't have a key
     * attached to the cert, so for truststores you almost certainly want JKS.
     *
     * @return keystore type for truststore.
     */
    @Config({"ness.httpclient.${httpclient_name}.ssl.truststore.type", "ness.httpclient.ssl.truststore.type"})
    @Default("JKS")
    public String getSSLTruststoreType()
    {
        return "JKS";
    }

    @Config(
        {"ness.httpclient.${httpclient_name}.ssl.server-cert-verification",
            "ness.httpclient.ssl.server-cert-verification"})
    @Default("true")
    public boolean useSSLServerCertVerification()
    {
        return true;
    }

    /**
     * This would be used for client-side certificates that are presented to the server.
     *
     * You must specify a password if you specify a keystore.
     *
     * @return Location of the SSL keystore.
     */
    @Config({"ness.httpclient.${httpclient_name}.ssl.keystore", "ness.httpclient.ssl.keystore"})
    @DefaultNull
    public String getSSLKeystore()
    {
        return null;
    }

    /**
     * You must specify a password if you specify a keystore.
     *
     * @return Password for the SSL keystore.
     */
    @Config({"ness.httpclient.${httpclient_name}.ssl.keystore.password", "ness.httpclient.ssl.keystore.password"})
    @DefaultNull
    public String getSSLKeystorePassword()
    {
        return null;
    }

    /**
     * Can be "JKS" or "PKCS12".
     *
     * @return keystore type for keystore.
     */
    @Config({"ness.httpclient.${httpclient_name}.ssl.keystore.type", "ness.httpclient.ssl.keystore.type"})
    @Default("JKS")
    public String getSSLKeystoreType()
    {
        return "JKS";
    }

    @Config({"ness.httpclient.${httpclient_name}.accept-encoding", "ness.httpclient.accept-encoding"})
    @Default("lz4,gzip,deflate")
    public String getDefaultAcceptEncoding()
    {
        return "lz4,gzip,deflate";
    }
}
