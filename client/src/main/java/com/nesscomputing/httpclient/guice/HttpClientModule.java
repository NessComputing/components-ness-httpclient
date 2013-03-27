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


import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import com.nesscomputing.config.ConfigProvider;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.HttpClientDefaults;
import com.nesscomputing.httpclient.HttpClientObserver;
import com.nesscomputing.httpclient.HttpClientObserverGroup;
import com.nesscomputing.httpclient.factory.httpclient4.ApacheHttpClient4Factory;
import com.nesscomputing.httpclient.internal.HttpClientFactory;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.AbstractLifecycleProvider;
import com.nesscomputing.lifecycle.guice.LifecycleAction;
import com.nesscomputing.logging.Log;

/**
 * Guice module to bind an instance of a HttpClient. Each HttpClient should be annotated or named so that
 * other pieces of code can request a specific instance of the HttpClient.
 */
public class HttpClientModule extends AbstractModule
{
    private static final Log LOG = Log.findLog();

    private static final String OBSERVER_GROUP = "__observer_groups";
    private static final String INHERIT_MAP = "__observer_group_inherit";

    private final String clientName;
    private final Set<HttpClientObserverGroup> observerGroups;

    /**
     * Bind an named HttpClient instance.
     * @param clientName The name to use. This can be used with the Named annotation in other parts of the code.
     */
    public HttpClientModule(@Nonnull final String clientName, @Nonnull final HttpClientObserverGroup... observerGroups)
    {
        Preconditions.checkArgument(clientName != null, "client name can not be null");

        this.clientName = clientName;
        this.observerGroups = ImmutableSet.copyOf(observerGroups);
    }

    @Override
    public void configure()
    {
        final Map<String, String> optionMap = Collections.singletonMap("httpclient_name", clientName);
        final Annotation annotation = Names.named(clientName);

        bind(HttpClientDefaults.class).annotatedWith(annotation).toProvider(ConfigProvider.of(null, HttpClientDefaults.class, optionMap)).in(Scopes.SINGLETON);
        bind(HttpClientFactory.class).annotatedWith(annotation).toProvider(new ApacheHttpClient4FactoryProvider(annotation, observerGroups)).in(Scopes.SINGLETON);
        bind(HttpClient.class).annotatedWith(annotation).toProvider(new HttpClientProvider(annotation)).asEagerSingleton();

        MapBinder.newMapBinder(binder(), HttpClientObserverGroup.class, HttpClientObserver.class, Names.named(OBSERVER_GROUP)).permitDuplicates();
        MapBinder.newMapBinder(binder(), HttpClientObserverGroup.class, HttpClientObserverGroup.class, Names.named(INHERIT_MAP)).permitDuplicates();
    }

    /**
     * Register a HttpClientObserver which observes *every* Guice-bound HttpClient.
     * @return the binding builder you should register with
     */
    public static LinkedBindingBuilder<HttpClientObserver> bindNewObserver(final Binder binder)
    {
        return Multibinder.newSetBinder(binder, HttpClientObserver.class).addBinding();
    }

    /**
     * Register a HttpClientObserver which observes only requests from a HttpClient with the given Guice binding annotation.
     * @return the binding builder you should register with
     */
    public static LinkedBindingBuilder<HttpClientObserver> bindNewObserver(final Binder binder, final Annotation annotation)
    {
        return Multibinder.newSetBinder(binder, HttpClientObserver.class, annotation).addBinding();
    }

    /**
     * Register a HttpClientObserver which observes requests from any HttpClient that is given the specified
     * {@link HttpClientObserverGroup}.
     */
    public static LinkedBindingBuilder<HttpClientObserver> bindNewObserver(final Binder binder, final HttpClientObserverGroup observerGroup)
    {
        return MapBinder.newMapBinder(binder, HttpClientObserverGroup.class, HttpClientObserver.class, Names.named(OBSERVER_GROUP)).addBinding(observerGroup);
    }

    public static void addObserverGroupInheritance(final Binder binder, final HttpClientObserverGroup subGroup, final HttpClientObserverGroup superGroup)
    {
        MapBinder.newMapBinder(binder, HttpClientObserverGroup.class, HttpClientObserverGroup.class, Names.named(INHERIT_MAP)).addBinding(subGroup).toInstance(superGroup);
    }

    /**
     * Provides an instance of a HttpClient. Retrieves all its dependencies based off an annotation.
     */
    static final class HttpClientProvider extends AbstractLifecycleProvider<HttpClient> implements Provider<HttpClient>
    {
        private HttpClientDefaults httpClientDefaults = null;
        private HttpClientFactory httpClientFactory = null;

        private final Annotation annotation;

        private HttpClientProvider(@Nonnull final Annotation annotation)
        {
            this.annotation = annotation;

            addAction(LifecycleStage.START_STAGE, new LifecycleAction<HttpClient>() {
                @Override
                public void performAction(final HttpClient httpClient) {
                    httpClient.start();
                }
            });

            addAction(LifecycleStage.STOP_STAGE, new LifecycleAction<HttpClient>() {
                @Override
                public void performAction(final HttpClient httpClient) {
                    httpClient.stop();
                }
            });
        }

        @Inject
        public void setInjector(final Injector injector)
        {
            this.httpClientDefaults = injector.getInstance(Key.get(HttpClientDefaults.class, annotation));
            this.httpClientFactory = injector.getInstance(Key.get(HttpClientFactory.class, annotation));
        }

        @Override
        public HttpClient internalGet()
        {
            return new HttpClient(httpClientFactory, httpClientDefaults);
        }
    }

    static final class ApacheHttpClient4FactoryProvider implements Provider<HttpClientFactory>
    {
        private static final TypeLiteral<Set<HttpClientObserver>> OBSERVER_TYPE_LITERAL = new TypeLiteral<Set<HttpClientObserver>>() {};
        private final Annotation annotation;
        private final Set<HttpClientObserverGroup> observerGroups;

        private Injector injector;
        private Map<HttpClientObserverGroup, Set<HttpClientObserver>> groupObserverMap;
        private Map<HttpClientObserverGroup, Set<HttpClientObserverGroup>> groupInheritanceMap;

        private ApacheHttpClient4FactoryProvider(@Nonnull final Annotation annotation, final Set<HttpClientObserverGroup> observerGroups)
        {
            this.annotation = annotation;
            this.observerGroups = observerGroups;
        }

        @Inject
        void setInjector(final Injector injector, @Named(OBSERVER_GROUP) Map<HttpClientObserverGroup, Set<HttpClientObserver>> groupObserverMap, @Named(INHERIT_MAP) Map<HttpClientObserverGroup, Set<HttpClientObserverGroup>> groupInheritanceMap)
        {
            this.injector = injector;
            this.groupObserverMap = groupObserverMap;
            this.groupInheritanceMap = groupInheritanceMap;
        }

        private Set<HttpClientObserver> findObservers(final Key<Set<HttpClientObserver>> key)
        {
            if (injector.getExistingBinding(key) != null) {
                return injector.getInstance(key);
            }
            else {
                return Collections.emptySet();
            }
        }

        private Set<HttpClientObserver> findObserversForGroups()
        {
            // Keep track of seen groups so that we cannot recurse infinitely if there is a cycle
            final Set<HttpClientObserverGroup> seen = new HashSet<>();
            final Set<HttpClientObserver> result = new HashSet<>();

            for (HttpClientObserverGroup group : observerGroups) {
                result.addAll(findObservers(group, seen));
            }

            return result;
        }

        private Set<HttpClientObserver> findObservers(HttpClientObserverGroup group, Set<HttpClientObserverGroup> seen)
        {
            if (!seen.add(group)) {
                return Collections.emptySet();
            }

            HashSet<HttpClientObserver> result = new HashSet<>();
            Set<HttpClientObserver> observers = groupObserverMap.get(group);
            if (observers != null) {
                result.addAll(observers);
            }

            Set<HttpClientObserverGroup> inheritGroups = groupInheritanceMap.get(group);
            if (inheritGroups != null) {
                for (HttpClientObserverGroup superGroup : inheritGroups) {
                    result.addAll(findObservers(superGroup, seen));
                }
            }

            return result;
        }

        @Override
        public HttpClientFactory get()
        {
            final Set<HttpClientObserver> httpClientObservers = ImmutableSet.<HttpClientObserver>builder()
                    .addAll(findObservers(Key.get(OBSERVER_TYPE_LITERAL)))
                    .addAll(findObservers(Key.get(OBSERVER_TYPE_LITERAL, annotation)))
                    .addAll(findObserversForGroups())
                    .build();

            LOG.info("HttpClient '%s' has observers: %s", httpClientObservers);

            final HttpClientDefaults httpClientDefaults = injector.getInstance(Key.get(HttpClientDefaults.class, annotation));
            return new ApacheHttpClient4Factory(httpClientDefaults, httpClientObservers);
        }
    }
}
