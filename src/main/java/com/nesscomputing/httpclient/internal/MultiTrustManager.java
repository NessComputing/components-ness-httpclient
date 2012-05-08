package com.nesscomputing.httpclient.internal;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

import com.google.common.collect.Sets;

public class MultiTrustManager implements X509TrustManager
{
    private final Set<X509TrustManager> trustManagers = Sets.newHashSet();

    public void addTrustManager(final X509TrustManager trustManager)
    {
        trustManagers.add(trustManager);
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException
    {
        if (trustManagers.isEmpty()) {
            throw new CertificateException("No trust managers installed!");
        }

        CertificateException ce = null;
        for (X509TrustManager trustManager : trustManagers) {
            try {
                trustManager.checkClientTrusted(chain, authType);
                return;
            }
            catch (CertificateException trustCe) {
                ce = trustCe;
            }
        }

        throw ce;
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException
    {
        if (trustManagers.isEmpty()) {
            throw new CertificateException("No trust managers installed!");
        }

        CertificateException ce = null;
        for (X509TrustManager trustManager : trustManagers) {
            try {
                trustManager.checkServerTrusted(chain, authType);
                return;
            }
            catch (CertificateException trustCe) {
                ce = trustCe;
            }
        }

        throw ce;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        final Set<X509Certificate> certificates = Sets.newHashSet();
        for (X509TrustManager trustManager : trustManagers) {
            certificates.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
        }
        return certificates.toArray(new X509Certificate [certificates.size()]);
    }
}
