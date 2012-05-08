package com.nesscomputing.httpclient.internal;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * A wrapper around another X509TrustManager that always trusts server certificates.
 */
public class AlwaysTrustServerTrustManager implements X509TrustManager {

    private final X509TrustManager innerTM;

    public AlwaysTrustServerTrustManager(X509TrustManager innerTM) {
        this.innerTM = innerTM;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        this.innerTM.checkClientTrusted(x509Certificates, s);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        // no op: always trust
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.innerTM.getAcceptedIssuers();
    }
}
