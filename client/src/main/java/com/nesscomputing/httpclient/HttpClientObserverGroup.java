package com.nesscomputing.httpclient;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;

/**
 * An observer group defines a common set of {@link HttpClientObserver}s shared by any number of HttpClient binding names.
 * This is useful when you decide that e.g. all {@code PLATFORM_INTERNAL} HttpClient types should provide a
 * certain type of authentication and tracking, but do not want to maintain the complete list of observers
 * for every internal client.  The class provides a few common groups which do not have better homes that you may
 * find useful.  These are not special in any way and any custom ones should usually be defined in the
 * appropriate module rather than here.
 */
public final class HttpClientObserverGroup
{
    /** Used for HttpClients that talk only to platform-internal services. */
    public static final HttpClientObserverGroup PLATFORM_INTERNAL = HttpClientObserverGroup.of("PLATFORM_INTERNAL");

    private final String name;

    private HttpClientObserverGroup(String name)
    {
        Preconditions.checkArgument(!StringUtils.isBlank(name), "blank observer group name!");
        this.name = name;
    }

    public static HttpClientObserverGroup of(String name)
    {
        return new HttpClientObserverGroup(name);
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof HttpClientObserverGroup && name.equals(((HttpClientObserverGroup) obj).name);
    }
}
