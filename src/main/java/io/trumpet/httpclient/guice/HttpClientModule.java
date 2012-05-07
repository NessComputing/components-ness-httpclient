package io.trumpet.httpclient.guice;

import io.trumpet.httpclient.HttpClient;
import io.trumpet.httpclient.HttpClientDefaults;
import io.trumpet.httpclient.HttpClientObserver;
import io.trumpet.httpclient.factory.httpclient4.ApacheHttpClient4Factory;
import io.trumpet.httpclient.internal.HttpClientFactory;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.nesscomputing.config.ConfigProvider;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.AbstractLifecycleProvider;
import com.nesscomputing.lifecycle.guice.LifecycleAction;

/**
 * Guice module to bind an instance of a HttpClient. Each HttpClient should be annotated or named so that
 * other pieces of code can request a specific instance of the HttpClient.
 */
public class HttpClientModule extends AbstractModule
{
    private final String clientName;

    /**
     * Bind an anonymous, unannotated HttpClient.  Guice 3.0 actually seems to get it right that this is not
     * exposed for arbitrary requests with annotations.
     */
    public HttpClientModule()
    {
        this.clientName = null;
    }

    /**
     * Bind an named HttpClient instance.
     * @param clientName The name to use. This can be used with the Named annotation in other parts of the code.
     */
    public HttpClientModule(final String clientName)
    {
        this.clientName = clientName;
    }

    @Override
    public void configure()
    {
        final ImmutableMap<String, String> optionMap = ImmutableMap.of("httpclient_name", Objects.firstNonNull(clientName, "default"));

        if (clientName != null) {
            final Annotation annotation = Names.named(clientName);

            bind(HttpClientDefaults.class).annotatedWith(annotation).toProvider(ConfigProvider.of(null, HttpClientDefaults.class, optionMap)).in(Scopes.SINGLETON);
            bind(HttpClientFactory.class).annotatedWith(annotation).toProvider(new ApacheHttpClient4FactoryProvider(clientName)).in(Scopes.SINGLETON);
            bind(HttpClient.class).annotatedWith(annotation).toProvider(new HttpClientProvider(clientName)).asEagerSingleton();
        }
        else {
            bind(HttpClientDefaults.class).toProvider(ConfigProvider.of(null, HttpClientDefaults.class, optionMap)).in(Scopes.SINGLETON);
            bind(HttpClientFactory.class).toProvider(new ApacheHttpClient4FactoryProvider(clientName)).in(Scopes.SINGLETON);
            // uses lifecycle, so bind eagerly to ensure it's registered itself by the time lifecycle runs
            bind(HttpClient.class).toProvider(new HttpClientProvider(clientName)).asEagerSingleton();
        }
    }

    /**
     * Register a HttpClient observer which observes *every* Guice-bound HttpClient.
     * @return the binding builder you should register with
     */
    public static LinkedBindingBuilder<HttpClientObserver> bindNewObserver(final Binder binder)
    {
        return Multibinder.newSetBinder(binder, HttpClientObserver.class).addBinding();
    }

    /**
     * Register a HttpClient observer which observes only requests from a HttpClient with the given Guice binding annotation.
     * @return the binding builder you should register with
     */
    public static LinkedBindingBuilder<HttpClientObserver> bindNewObserver(final Binder binder, final Annotation annotation)
    {
        return Multibinder.newSetBinder(binder, HttpClientObserver.class, annotation).addBinding();
    }

    /**
     * Provides an instance of a HttpClient. Retrieves all its dependencies based off an annotation.
     */
    static final class HttpClientProvider extends AbstractLifecycleProvider<HttpClient> implements Provider<HttpClient>
    {
        private HttpClientDefaults  httpClientDefaults = null;
        private HttpClientFactory httpClientFactory = null;

        private final Annotation annotation;

        HttpClientProvider(@Nullable final String clientName)
        {
            this.annotation = clientName != null ? Names.named(clientName) : null;

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
            if (annotation != null) {
                this.httpClientDefaults = injector.getInstance(Key.get(HttpClientDefaults.class, annotation));
                this.httpClientFactory = injector.getInstance(Key.get(HttpClientFactory.class, annotation));
            } else {
                this.httpClientDefaults = injector.getInstance(HttpClientDefaults.class);
                this.httpClientFactory = injector.getInstance(HttpClientFactory.class);
            }
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

        private HttpClientDefaults httpClientDefaults = null;
        private Set<HttpClientObserver> httpClientObservers = null;

        ApacheHttpClient4FactoryProvider(@Nullable final String clientName)
        {
            this.annotation = clientName != null ? Names.named(clientName) : null;
        }

        @Inject
        public void setInjector(final Injector injector)
        {
            httpClientObservers = Sets.newHashSet();

            httpClientObservers.addAll(findObservers(injector, Key.get(OBSERVER_TYPE_LITERAL)));

            if (annotation != null) {
                httpClientObservers.addAll(findObservers(injector, Key.get(OBSERVER_TYPE_LITERAL, annotation)));
                httpClientDefaults = injector.getInstance(Key.get(HttpClientDefaults.class, annotation));
            }
            else {
                httpClientDefaults = injector.getInstance(HttpClientDefaults.class);
            }
        }

        private Set<HttpClientObserver> findObservers(final Injector injector, final Key<Set<HttpClientObserver>> key)
        {
            if (injector.getExistingBinding(key) != null) {
                return injector.getInstance(key);
            }
            else {
                return Collections.emptySet();
            }
        }

        @Override
        public HttpClientFactory get()
        {
            return new ApacheHttpClient4Factory(httpClientDefaults, httpClientObservers);
        }
    }
}
