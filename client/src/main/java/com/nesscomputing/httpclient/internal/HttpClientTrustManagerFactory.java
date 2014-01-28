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
package com.nesscomputing.httpclient.internal;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;

import com.nesscomputing.httpclient.HttpClientDefaults;
import com.nesscomputing.logging.Log;

import org.apache.commons.lang3.StringUtils;

public final class HttpClientTrustManagerFactory {
    private static final Log LOG = Log.findLog();

    private HttpClientTrustManagerFactory() {
    }

    @Nonnull
    public static X509KeyManager getKeyManager(String keystorePath,
                                               String keystoreType,
                                               String keystorePassword)
        throws IOException, GeneralSecurityException
    {
        Preconditions.checkArgument(keystorePath != null, "keystore path must not be null!");
        Preconditions.checkArgument(keystoreType != null, "keystore type must not be null!");
        Preconditions.checkArgument(keystorePassword != null, "keystore password must not be null!");
        KeyStore keyStore = loadKeystore(keystorePath, keystoreType, keystorePassword);
        return getKeyManagerForKeystore(keyStore, keystorePassword);
    }

    @Nonnull
    public static X509TrustManager getDefaultTrustManager() throws GeneralSecurityException {
        return trustManagerFromKeystore(null);
    }

    @Nonnull
    public static X509TrustManager getTrustManagerForHttpClientDefaults(
        final HttpClientDefaults clientDefaults) throws GeneralSecurityException, IOException {
        final KeyStore keystore =
            loadKeystore(clientDefaults.getSSLTruststore(), clientDefaults.getSSLTruststoreType(),
                clientDefaults.getSSLTruststorePassword());

        return trustManagerFromKeystore(keystore);
    }

    @Nonnull
    private static KeyStore loadKeystore(@Nonnull String location, @Nonnull String keystoreType,
        @Nonnull String keystorePassword) throws GeneralSecurityException, IOException {
        final KeyStore keystore = KeyStore.getInstance(keystoreType);
        URL keystoreUrl;
        if (StringUtils.startsWithIgnoreCase(location, "classpath:")) {
            keystoreUrl =
                Resources.getResource(HttpClientTrustManagerFactory.class, location.substring(10));
        } else {
            keystoreUrl = new URL(location);
        }
        keystore.load(keystoreUrl.openStream(), keystorePassword.toCharArray());
        return keystore;
    }

    @Nonnull
    private static X509KeyManager getKeyManagerForKeystore(@Nonnull KeyStore keyStore,
        @Nonnull String password) throws GeneralSecurityException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");

        keyManagerFactory.init(keyStore, password.toCharArray());

        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509KeyManager) {
                return (X509KeyManager) keyManager;
            }
        }

        throw new IllegalStateException("Couldn't find an X509KeyManager");
    }

    @Nonnull
    private static X509TrustManager trustManagerFromKeystore(final KeyStore keystore)
        throws GeneralSecurityException {
        final TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance("PKIX", "SunJSSE");
        trustManagerFactory.init(keystore);

        final TrustManager[] tms = trustManagerFactory.getTrustManagers();

        for (TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                final X509TrustManager manager = (X509TrustManager) tm;
                X509Certificate[] acceptedIssuers = manager.getAcceptedIssuers();
                LOG.debug("Found TrustManager with %d authorities.", acceptedIssuers.length);
                for (int i = 0; i < acceptedIssuers.length; i++) {
                    X509Certificate issuer = acceptedIssuers[i];

                    LOG.trace("Issuer #%d, subject DN=<%s>, serial=<%s>", i, issuer.getSubjectDN(), issuer.getSerialNumber());
                }

                return manager;
            }
        }
        throw new IllegalStateException("Could not find an X509TrustManager");
    }
}
