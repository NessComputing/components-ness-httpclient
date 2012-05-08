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
import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.httpclient.HttpClient;
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
    public void testMultipleWithLifecycle()
    {
        final Config config = Config.getFixedConfig("trumpet.httpclient.user-agent", "default",
                                                    "trumpet.httpclient.testing.user-agent", "testing",
                                                    "trumpet.httpclient.running.user-agent", "running");

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(config),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule("testing"),
                                                       new HttpClientModule("running"));

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        final HttpClient testingHttpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("testing")));
        final HttpClient runningHttpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("running")));


        Assert.assertNotNull(testingHttpClient);
        Assert.assertNotNull(runningHttpClient);
        Assert.assertFalse(testingHttpClient == runningHttpClient);

        Assert.assertFalse(testingHttpClient.isStarted());
        Assert.assertFalse(runningHttpClient.isStarted());
        Assert.assertFalse(testingHttpClient.isStopped());
        Assert.assertFalse(runningHttpClient.isStopped());

        lifecycle.executeTo(LifecycleStage.START_STAGE);

        Assert.assertTrue(testingHttpClient.isStarted());
        Assert.assertTrue(runningHttpClient.isStarted());
        Assert.assertFalse(testingHttpClient.isStopped());
        Assert.assertFalse(runningHttpClient.isStopped());

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(testingHttpClient.isStarted());
        Assert.assertTrue(runningHttpClient.isStarted());
        Assert.assertTrue(testingHttpClient.isStopped());
        Assert.assertTrue(runningHttpClient.isStopped());
    }

    @Test
    public void testMultipleWithLifecycleAndDefault()
    {
        final Config config = Config.getFixedConfig("trumpet.httpclient.user-agent", "default");

        final Injector injector = Guice.createInjector(Stage.PRODUCTION,
                                                       new ConfigModule(config),
                                                       ENFORCEMENT_MODULE,
                                                       new LifecycleModule(),
                                                       new HttpClientModule(),
                                                       new HttpClientModule("running"));

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        final HttpClient defaultHttpClient = injector.getInstance(Key.get(HttpClient.class));
        final HttpClient runningHttpClient = injector.getInstance(Key.get(HttpClient.class, Names.named("running")));


        Assert.assertNotNull(defaultHttpClient);
        Assert.assertNotNull(runningHttpClient);
        Assert.assertFalse(defaultHttpClient == runningHttpClient);

        Assert.assertFalse(defaultHttpClient.isStarted());
        Assert.assertFalse(runningHttpClient.isStarted());
        Assert.assertFalse(defaultHttpClient.isStopped());
        Assert.assertFalse(runningHttpClient.isStopped());

        lifecycle.executeTo(LifecycleStage.START_STAGE);

        Assert.assertTrue(defaultHttpClient.isStarted());
        Assert.assertTrue(runningHttpClient.isStarted());
        Assert.assertFalse(defaultHttpClient.isStopped());
        Assert.assertFalse(runningHttpClient.isStopped());

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertTrue(defaultHttpClient.isStarted());
        Assert.assertTrue(runningHttpClient.isStarted());
        Assert.assertTrue(defaultHttpClient.isStopped());
        Assert.assertTrue(runningHttpClient.isStopped());
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






