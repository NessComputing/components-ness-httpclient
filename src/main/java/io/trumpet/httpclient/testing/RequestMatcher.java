package io.trumpet.httpclient.testing;

import io.trumpet.httpclient.HttpClientRequest;

import com.google.common.base.Predicate;

public interface RequestMatcher extends Predicate<HttpClientRequest<?>> { }
