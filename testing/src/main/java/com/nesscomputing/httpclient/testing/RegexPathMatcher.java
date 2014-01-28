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
package com.nesscomputing.httpclient.testing;


import java.util.regex.Pattern;

import com.nesscomputing.httpclient.HttpClientRequest;
import com.nesscomputing.httpclient.internal.HttpClientMethod;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
        if (!(other instanceof RegexPathMatcher)) {
            return false;
        }
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
