package com.nesscomputing.httpclient.guice;


import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.guice.HttpClientModule;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;

public class TestGuiceModule
{
    private static final Module ENFORCEMENT_MODULE = new Module() {
        @Override
        public void configure(final Binder binder) {
            binder.disableCircularProxies();
            binder.requireExplicitBindings();
        }
    };

    @Test
    public void testDefault()
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       ConfigModule.forTesting(),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule());

        final HttpClient httpClient = injector.getInstance(HttpClient.class);

        Assert.assertNotNull(httpClient);
    }

    @Test(expected = ConfigurationException.class)
    public void testDefaultIsBad()
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       ConfigModule.forTesting(),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule());

        injector.getInstance(Key.get(HttpClient.class, Names.named("_some_strange_thing")));
    }

    @Test
    public void testGetNamed()
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       ConfigModule.forTesting(),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule("testing"));

        final HttpClient httpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("testing")));

        Assert.assertNotNull(httpClient);
    }

    @Test
    public void testMultiple()
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       ConfigModule.forTesting(),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule("testing"),
                                                       new HttpClientModule("running"));

        final HttpClient testingHttpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("testing")));
        final HttpClient runningHttpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("running")));

        Assert.assertNotNull(testingHttpClient);
        Assert.assertNotNull(runningHttpClient);
        Assert.assertFalse(testingHttpClient == runningHttpClient);
    }

    @Test
    public void testLifecycle() throws Exception
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       ConfigModule.forTesting(),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule("testing"));

        final HttpClient httpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("testing")));
        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        Assert.assertNotNull(httpClient);

        lifecycle.executeTo(LifecycleStage.START_STAGE);

        Thread.sleep(1000L);

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);
    }

    @Test
    public void testNonstopLifecycle() throws Exception
    {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       ConfigModule.forTesting(),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule("testing"));

        final HttpClient httpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("testing")));
        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        Assert.assertNotNull(httpClient);

        lifecycle.executeTo(LifecycleStage.START_STAGE);

    }
}






