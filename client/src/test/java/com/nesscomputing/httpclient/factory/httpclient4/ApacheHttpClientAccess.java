package com.nesscomputing.httpclient.factory.httpclient4;

import java.util.Set;

import com.nesscomputing.httpclient.HttpClientObserver;

public class ApacheHttpClientAccess
{
    public static Set<? extends HttpClientObserver> getObservers(ApacheHttpClient4Factory factory)
    {
        return factory.getHttpClientObservers();
    }
}
