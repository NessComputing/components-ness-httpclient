package io.trumpet.httpclient.testing;

import io.trumpet.httpclient.HttpClientRequest;
import io.trumpet.httpclient.internal.HttpClientMethod;

import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Matches a regex (or any regex if null) and a regexp for the path.
 */
class RegexPathMatcher implements RequestMatcher
{
    private final HttpClientMethod method;
    private final Pattern pattern;

    public RegexPathMatcher(final HttpClientMethod method, final String pattern)
    {
        this.method = method;
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean apply(HttpClientRequest<?> input)
    {
        if (method != null && !method.equals(input.getHttpMethod())) {
            return false;
        }

        return pattern.matcher(input.getUri().getPath()).matches();
   }

    @Override
    public boolean equals(final Object other)
    {
        if (!(other instanceof RegexPathMatcher))
            return false;
        RegexPathMatcher castOther = (RegexPathMatcher) other;
        return new EqualsBuilder().append(method, castOther.method).append(pattern, castOther.pattern).isEquals();
    }

    private transient int hashCode;

    @Override
    public int hashCode()
    {
        if (hashCode == 0) {
            hashCode = new HashCodeBuilder().append(method).append(pattern).toHashCode();
        }
        return hashCode;
    }

    private transient String toString;

    @Override
    public String toString()
    {
        if (toString == null) {
            toString = new ToStringBuilder(this).append("method", method).append("pattern", pattern).toString();
        }
        return toString;
    }


}
