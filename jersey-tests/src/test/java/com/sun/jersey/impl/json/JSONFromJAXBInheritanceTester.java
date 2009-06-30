/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl.json;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Jakub.Podlesak@Sun.Com
 */
public class JSONFromJAXBInheritanceTester extends AbstractResourceTester {
    public JSONFromJAXBInheritanceTester(String testName) {
        super(testName);
    }

    @Provider
    public static class JAXBContextResolver implements ContextResolver<JAXBContext> {
        private JAXBContext context;
        private final Class[] cTypes = {Animal.class, AnimalList.class, Dog.class, Cat.class};
        private final Set<Class> types;
        public JAXBContextResolver() {
            try {
                this.types = new HashSet<Class>(Arrays.asList(cTypes));
                this.context = new JSONJAXBContext(JSONConfiguration.mapped().rootUnwrapping(false).build(), cTypes);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        public JAXBContext getContext(Class<?> c) {
            return types.contains(c) ? context : null;
        }
    }
    
    @Path("/")
    public static class AnimalResource {
        @POST @Consumes("application/json") @Produces("application/json")
        public Animal get(Animal b) {
            return b;
        }
    }
    
    public void testAnimalResource() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        ResourceConfig rc = new DefaultResourceConfig(AnimalResource.class);
        rc.getSingletons().add(cr);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());
        initiateWebApplication(rc);

        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(cr.getClass());
        WebResource r = resource("/", cc);
        System.out.println(r.type(MediaType.APPLICATION_JSON).post(String.class, new Animal("bobik animal")));
        assertEquals("bobik animal", r.type("application/json").post(Animal.class, new Animal("bobik animal")).name);
        assertEquals(Cat.class, r.type(MediaType.APPLICATION_JSON).post(Animal.class, new Cat("bobik cat")).getClass());
        assertEquals(Dog.class, r.type(MediaType.APPLICATION_JSON).post(Animal.class, new Dog("bobik dog")).getClass());
    }        
}