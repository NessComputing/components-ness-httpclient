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
package com.nesscomputing.httpclient.testsupport;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;

import javax.security.auth.Subject;

import com.google.common.io.Resources;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.MappedLoginService.KnownUser;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.kitei.testing.lessio.AllowNetworkListen;

@AllowNetworkListen(ports={0})
public class LocalHttpService
{
    private final Server server = new Server();
    private final Connector connector;

    public static LocalHttpService forHandler(final Handler handler)
    {
        return new LocalHttpService(handler, getHttpConnector());
    }

    public static LocalHttpService forSecureHandler(final Handler handler, final String login, final String password)
    {
        return new LocalHttpService(getSecuredHandler(handler, login, password), getHttpConnector());
    }

    public static LocalHttpService forSSLHandler(final Handler handler)
    {
        return new LocalHttpService(handler, getSSLHttpConnector());
    }

    public static LocalHttpService forSecureSSLHandler(final Handler handler, final String login, final String password)
    {
        return new LocalHttpService(getSecuredHandler(handler, login, password), getSSLHttpConnector());
    }

	public static LocalHttpService forSSLClientSSLServerHandler(final Handler handler,
		String truststore, String truststorePassword, String keystore, String keystorePassword,
		String keystoreType) {
		return new LocalHttpService(handler,
			getSSLClientCertHttpConnector(truststore, truststorePassword, keystore,
				keystorePassword, keystoreType));
	}

    private static Connector getHttpConnector()
    {
        final SelectChannelConnector scc = new SelectChannelConnector();
        scc.setPort(0);
        scc.setHost("localhost");
        return scc;
    }

    private static Connector getSSLHttpConnector()
    {
        final URL keystoreUrl = Resources.getResource(LocalHttpService.class, "/ssl-server-keystore.jks");

        final SslContextFactory contextFactory = new SslContextFactory();

        contextFactory.setKeyStorePath(keystoreUrl.toString());
        contextFactory.setKeyStorePassword("changeit");
        contextFactory.setKeyManagerPassword("changeit");
        final SslSelectChannelConnector scc = new SslSelectChannelConnector(contextFactory);
        scc.setPort(0);
        scc.setHost("localhost");

        return scc;
    }

	private static Connector getSSLClientCertHttpConnector(String truststore,
		String truststorePassword, String keystore, String keystorePassword, String keystoreType)
	{
        final URL keystoreUrl = Resources.getResource(LocalHttpService.class, keystore);

        final SslContextFactory contextFactory = new SslContextFactory();
        contextFactory.setKeyStorePath(keystoreUrl.toString());
        contextFactory.setKeyStorePassword(keystorePassword);
        contextFactory.setKeyManagerPassword(keystorePassword);
        contextFactory.setKeyStoreType(keystoreType);

        final URL truststoreUrl = Resources.getResource(LocalHttpService.class, truststore);
        contextFactory.setTrustStore(truststoreUrl.toString());
        contextFactory.setTrustStorePassword(truststorePassword);
        contextFactory.setTrustStoreType("JKS");

        contextFactory.setNeedClientAuth(true);

        final SslSelectChannelConnector scc = new SslSelectChannelConnector(contextFactory);
		scc.setPort(0);
		scc.setHost("localhost");

		return scc;
	}

    private static Handler getSecuredHandler(final Handler handler, final String login, final String password)
    {
        final SecurityHandler securityHandler = new DummySecurityHandler(login);
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(new DummyLoginService(login, password));

        securityHandler.setHandler(handler);

        return securityHandler;
    }

    /**
     * @deprecated Use {@link LocalHttpService#forHandler(Handler)}.
     */
    @Deprecated
    public LocalHttpService(final Handler handler)
    {
        this(handler, getHttpConnector());
    }

    private LocalHttpService(final Handler handler, final Connector connector)
    {
        this.connector = connector;
        server.setConnectors(new Connector[] { connector });
        server.setHandler(handler);
    }

    public void start()
    {
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("LocalHttpService did not start properly!");
        }
    }

    public void stop()
    {
        try {
            server.stop();
        } catch (Exception e) {
            throw new IllegalStateException("LocalHttpService did not stop properly!");
        }

        try {
            server.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String getHost()
    {
        return connector.getHost();
    }

    public int getPort()
    {
        return connector.getLocalPort();
    }

    private static class DummySecurityHandler extends SecurityHandler
    {
        private final String login;

        private DummySecurityHandler(final String login)
        {
            this.login = login;
        }

        @Override
        protected Object prepareConstraintInfo(String pathInContext, Request request)
        {
            return null;
        }

        @Override
        protected boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException
        {
            return true;
        }

        @Override
        protected boolean isAuthMandatory(Request baseRequest, Response base_response, Object constraintInfo)
        {
            return true;
        }

        @Override
        protected boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo, UserIdentity userIdentity) throws IOException
        {
            return userIdentity != null && StringUtils.equals(login, userIdentity.getUserPrincipal().getName());
        }
    }

    private static class DummyLoginService implements LoginService
    {
        private final String login;
        private final String password;

        private DummyLoginService(final String login, final String password)
        {
            this.login = login;
            this.password = password;
        }

        @Override
        public String getName()
        {
            return "test";
        }

        @Override
        public UserIdentity login(String username, Object credentials)
        {
            if (StringUtils.equals(login, username) && StringUtils.equals(password, String.valueOf(credentials))) {
                final Credential c = new Password(String.valueOf(credentials));
                final Principal p = new KnownUser(username, c);
                final Subject subject = new Subject();
                subject.getPrincipals().add(p);
                subject.getPrivateCredentials().add(c);

                return new UserIdentity() {

                    @Override
                    public Subject getSubject()
                    {
                        return subject;
                    }

                    @Override
                    public Principal getUserPrincipal()
                    {
                        return p;
                    }

                    @Override
                    public boolean isUserInRole(String role, Scope scope)
                    {
                        return true;
                    }

                };
            }
            else {
                return null;
            }
        }

        @Override
        public void logout(UserIdentity user)
        {
        }

        @Override
        public boolean validate(UserIdentity user)
        {
            return false;
        }

        private IdentityService identityService;

        @Override
        public IdentityService getIdentityService()
        {
            return identityService;
        }

        @Override
        public void setIdentityService(IdentityService service)
        {
            this.identityService = service;
        }

    }
}
