package com.nesscomputing.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.easymock.EasyMock;
import org.junit.Test;

import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.httpclient.factory.httpclient4.ApacheHttpClient4Factory;
import com.nesscomputing.httpclient.factory.httpclient4.ApacheHttpClientAccess;
import com.nesscomputing.httpclient.guice.HttpClientModule;
import com.nesscomputing.httpclient.internal.HttpClientFactory;

public class TestObserverGroups
{

    private final HttpClientObserverGroup testGroup = HttpClientObserverGroup.of("testgroup");

    @Inject
    @Named("test")
    HttpClientFactory testFactory;

    private static HttpClientObserver createObserverMock()
    {
        HttpClientObserver mock = EasyMock.createMock(HttpClientObserver.class);
        EasyMock.replay(mock);
        return mock;
    }

    private Set<? extends HttpClientObserver> getObservers()
    {
        return ApacheHttpClientAccess.getObservers((ApacheHttpClient4Factory) testFactory);
    }

    @Test
    public void testNoGroups()
    {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());

                install (new HttpClientModule("test"));
            }
        }).injectMembers(this);

        assertTrue(getObservers().isEmpty());
    }

    @Test
    public void testUnusedGroups()
    {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());

                install (new HttpClientModule("test"));
                HttpClientModule.bindNewObserver(binder(), testGroup).toInstance(createObserverMock());
            }
        }).injectMembers(this);

        assertTrue(getObservers().isEmpty());
    }

    @Test
    public void testSimpleGroup()
    {
        final HttpClientObserver obs1 = createObserverMock();

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());

                install (new HttpClientModule("test", testGroup));
                HttpClientModule.bindNewObserver(binder(), testGroup).toInstance(obs1);
            }
        }).injectMembers(this);

        assertEquals(ImmutableSet.of(obs1), getObservers());
    }

    @Test
    public void testDoubleGroup()
    {
        final HttpClientObserver obs1 = createObserverMock();
        final HttpClientObserver obs2 = createObserverMock();

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());

                install (new HttpClientModule("test", testGroup));
                HttpClientModule.bindNewObserver(binder(), testGroup).toInstance(obs1);
                HttpClientModule.bindNewObserver(binder(), testGroup).toInstance(obs2);
            }
        }).injectMembers(this);

        assertEquals(ImmutableSet.of(obs1, obs2), getObservers());
    }

    @Test
    public void testSimpleInheritance()
    {
        final HttpClientObserver obs1 = createObserverMock();
        final HttpClientObserver obs2 = createObserverMock();

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());

                install (new HttpClientModule("test", testGroup));
                HttpClientModule.bindNewObserver(binder(), testGroup).toInstance(obs1);
                HttpClientModule.bindNewObserver(binder(), HttpClientObserverGroup.PLATFORM_INTERNAL).toInstance(obs2);
                HttpClientModule.addObserverGroupInheritance(binder(), testGroup, HttpClientObserverGroup.PLATFORM_INTERNAL);
            }
        }).injectMembers(this);

        assertEquals(ImmutableSet.of(obs1, obs2), getObservers());
    }

    @Test
    public void testComplexInheritance()
    {
        final HttpClientObserverGroup group1 = HttpClientObserverGroup.of("test1");
        final HttpClientObserverGroup group2 = HttpClientObserverGroup.of("test2");
        final HttpClientObserverGroup group3 = HttpClientObserverGroup.of("test3");
        final HttpClientObserver obs1 = createObserverMock();
        final HttpClientObserver obs2 = createObserverMock();

        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());

                install (new HttpClientModule("test", testGroup));
                HttpClientModule.bindNewObserver(binder(), group3).toInstance(obs1);
                HttpClientModule.bindNewObserver(binder(), group1).toInstance(obs2);
                HttpClientModule.addObserverGroupInheritance(binder(), testGroup, group1);
                HttpClientModule.addObserverGroupInheritance(binder(), group1, group2);
                HttpClientModule.addObserverGroupInheritance(binder(), group1, group3);
                HttpClientModule.addObserverGroupInheritance(binder(), group2, group3);
            }
        }).injectMembers(this);

        assertEquals(ImmutableSet.of(obs1, obs2), getObservers());
    }
}
