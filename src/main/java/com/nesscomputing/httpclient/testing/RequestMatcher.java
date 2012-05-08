package com.nesscomputing.httpclient.testing;


import com.google.common.base.Predicate;
import com.nesscomputing.httpclient.HttpClientRequest;

public interface RequestMatcher extends Predicate<HttpClientRequest<?>> { }
