/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.sun.jersey.api.client.Client;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class GuiceChildParentNoScopeSingletonTest extends AbstractGrizzlyServerTester {
    public GuiceChildParentNoScopeSingletonTest(String testName) {
        super(testName);
    }

    @Path("parent")
	public static class BoundInParentInjector {
		@Inject
		public BoundInParentInjector() {}

        @GET
        public String get(@Context ResourceContext rc) {
            return "PARENT " + (this == rc.getResource(BoundInParentInjector.class));
        }
	}

    @Path("child")
	public static class BoundInChildInjector {
		@Inject
		public BoundInChildInjector(BoundInParentInjector fromParentInjector) {}

        @GET
        public String get(@Context ResourceContext rc) {
            return "CHILD " + (this == rc.getResource(BoundInChildInjector.class));
        }
	}

	private ResourceConfig buildResourceConfig(Injector injector) {
		ResourceConfig resourceConfig = new DefaultResourceConfig();
		IoCComponentProviderFactory f = new GuiceComponentProviderFactory(resourceConfig, injector);
        resourceConfig.getSingletons().add(f);
        return resourceConfig;
	}

	private Injector withExplicitBindingFor(final Scope scope) {
		Injector parentInjector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule(){
			@Override
			protected void configure() {
				bind(BoundInParentInjector.class).in(scope);
			}
		});
		Injector childInjector = parentInjector.createChildInjector(new AbstractModule(){
			@Override
			protected void configure() {
				bind(BoundInChildInjector.class).in(scope);
			}
		});
		return childInjector;
	}

    public void testNoScope() {
        startServer(buildResourceConfig(withExplicitBindingFor(Scopes.NO_SCOPE)));
        WebResource r = Client.create().resource(getUri().path("/").build());
        assertEquals("CHILD false", r.path("child").get(String.class));
        assertEquals("PARENT false", r.path("parent").get(String.class));
    }
    
    public void testSingleton() {
        startServer(buildResourceConfig(withExplicitBindingFor(Scopes.SINGLETON)));
        WebResource r = Client.create().resource(getUri().path("/").build());
        assertEquals("CHILD true", r.path("child").get(String.class));
        assertEquals("PARENT true", r.path("parent").get(String.class));
    }

}
