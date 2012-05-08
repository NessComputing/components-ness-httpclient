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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Default configuration bean for the HTTP client. Can be configured using config-magic. */
public class HttpClientDefaults {
    /**
     * Default is 200.
     *
     * @return The maximum number of connections allowed.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.total-connections-max", "trumpet.httpclient.total-connections-max"})
    @Default("200")
    public int getTotalConnectionsMax() {
        return 200;
    }

    /**
     * Default is 20.
     *
     * @return The number of connections allowed to a single host/port combination.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.perhost-connections-max", "trumpet.httpclient.perhost-connections-max"})
    @Default("20")
    public int getPerHostConnectionsMax() {
        return 20;
    }

    /**
     * Default is 60000 (60 seconds).
     *
     * @return Timeout connecting to a remote host in milliseconds.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.connection-timeout", "trumpet.httpclient.connection-timeout"})
    @Default("60s")
    public TimeSpan getConnectionTimeout() {
        return new TimeSpan(60, TimeUnit.SECONDS);
    }

    /**
     * Default is an outrageously long 60s to match the existing outrageously long default request timeout
     *
     * @return  Socket timeout
     */
    @Config({"trumpet.httpclient.${httpclient_name}.socket-timeout", "trumpet.httpclient.socket-timeout"})
    @Default("60s")
    public TimeSpan getSocketTimeout() {
        return new TimeSpan(60, TimeUnit.SECONDS);
    }

    /**
     * Default is 15000 (15 seconds).
     *
     * @return Timeout for an established connection in keep-alive until it is closed. If 0,
     *         connections are closed immediately.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.idle-timeout", "trumpet.httpclient.idle-timeout"})
    @Default("15s")
    public TimeSpan getIdleTimeout() {
        return new TimeSpan(15, TimeUnit.SECONDS);
    }

    /**
     * Default is 60000 (60 seconds).
     *
     * @return Timeout for a request sent until a response must arrive.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.request-timeout", "trumpet.httpclient.request-timeout"})
    @Default("60s")
    public TimeSpan getRequestTimeout() {
        return new TimeSpan(60, TimeUnit.SECONDS);
    }

    /**
     * Default is true (follow redirects).
     *
     * @return true if redirects should be followed automatically.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.follow-redirects", "trumpet.httpclient.follow-redirects"})
    @Default("true")
    public boolean isFollowRedirects() {
        return true;
    }

    /**
     * Default is 5.
     *
     * @return Maximum number of redirects until an error is returned.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.max-redirects", "trumpet.httpclient.max-redirects"})
    @Default("5")
    public int getMaxRedirects() {
        return 5;
    }

    /**
     * Default is 'Trumpet HTTP Client'.
     *
     * @return Name of the user agent, used to identify to remote sites.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.user-agent", "trumpet.httpclient.user-agent"})
    @Default("Trumpet HTTP Client")
    public String getUserAgent() {
        return "Trumpet HTTP Client";
    }

    /**
     * Default is 3.
     *
     * @return Maximum number of retries for a request.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.retries", "trumpet.httpclient.retries"})
    @Default("3")
    public int getRetries() {
        return 3;
    }

    /**
     * This would be used to verify server certs that aren't signed by a standard CA.
     *
     * You must specify a password if you specify a truststore.
     *
     * @return Location of the SSL truststore.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.ssl.truststore", "trumpet.httpclient.ssl.truststore"})
    @Default("classpath:/default-truststore.jks")
    @Nullable
    public String getSSLTruststore() {
        return "classpath:/default-truststore.jks";
    }

    /**
     * You must specify a password if you specify a truststore. If either the truststore or truststore password is
     * null, the default truststore will be used.
     *
     * @return Password for the SSL truststore.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.ssl.truststore.password", "trumpet.httpclient.ssl.truststore.password"})
    @Default("changeit")
    @Nullable
    public String getSSLTruststorePassword() {
        return "changeit";
    }

    /**
     * This only applies if there is a custom truststore.
     *
     * @return true if the client should fall back to default truststore if the custom truststore can
     *         not validate a request.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.ssl.truststore.fallback", "trumpet.httpclient.ssl.truststore.fallback"})
    @Default("true")
    public boolean isSSLTruststoreFallback() {
        return true;
    }

    /**
     * Can be "JKS" or "PKCS12". Note that Java can't read PKCS12 files that don't have a key
     * attached to the cert, so for truststores you almost certainly want JKS.
     *
     * @return keystore type for truststore.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.ssl.truststore.type", "trumpet.httpclient.ssl.truststore.type"})
    @Default("JKS")
    @Nonnull
    public String getSSLTruststoreType() {
        return "JKS";
    }

    @Config(
        {"trumpet.httpclient.${httpclient_name}.ssl.server-cert-verification",
            "trumpet.httpclient.ssl.server-cert-verification"})
    @Default("true")
    public boolean isSSLServerCertVerification() {
        return true;
    }

    /**
     * This would be used for client-side certificates that are presented to the server.
     *
     * You must specify a password if you specify a keystore.
     *
     * @return Location of the SSL keystore.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.ssl.keystore", "trumpet.httpclient.ssl.keystore"})
    @DefaultNull
    @CheckForNull
    public String getSSLKeystore()
    {
        return null;
    }

    /**
     * You must specify a password if you specify a keystore.
     *
     * @return Password for the SSL keystore.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.ssl.keystore.password", "trumpet.httpclient.ssl.keystore.password"})
    @DefaultNull
    @CheckForNull
    public String getSSLKeystorePassword() {
        return null;
    }

    /**
     * Can be "JKS" or "PKCS12". Since keystores always have a key attached to a cert,
     * PKCS12 works fine here (doesn't hit SunJSSE's PKCS12 bug).
     *
     * @return keystore type for keystore.
     */
    @Config({"trumpet.httpclient.${httpclient_name}.ssl.keystore.type", "trumpet.httpclient.ssl.keystore.type"})
    @Default("PKCS12")
    @Nonnull
    public String getSSLKeystoreType() {
        return "PKCS12";
    }
}
