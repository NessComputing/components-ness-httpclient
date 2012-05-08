package com.nesscomputing.httpclient.testing;

import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.internal.HttpClientMethod;

/**
 * Simple {@link RequestMatcher} which checks for the path and method to be equal
 */
class MethodPathMatcher implements RequestMatcher {
    private final HttpClientMethod method;
    private final String path;
    public MethodPathMatcher(HttpClientMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    @Override
    public boolean apply(HttpClientRequest<?> input) {
        return input.getHttpMethod().equals(method) && input.getUri().getPath().equals(path);
    }

    @Override
    public String toString() {
        return String.format("MethodPathMatcher [method=%s, path=%s]", method, path);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodPathMatcher other = (MethodPathMatcher) obj;
        if (method != other.method)
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }
}
