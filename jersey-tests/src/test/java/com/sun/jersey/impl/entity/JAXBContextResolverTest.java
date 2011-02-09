/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.impl.entity;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class JAXBContextResolverTest extends AbstractResourceTester {
    public JAXBContextResolverTest(String testName) {
        super(testName);
    }
    
    @Provider
    public static class JAXBContextResolver implements ContextResolver<JAXBContext> {
        private JAXBContext context;
        private int invoked;
        public JAXBContextResolver() {
            try {
                this.context = JAXBContext.newInstance(JAXBBean.class);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        public JAXBContext getContext(Class<?> c) {
            if (JAXBBean.class == c) {
                invoked++;
                return context;
            } else 
                return null;
        }
        
        public int invoked() {
            return invoked;
        }
    }
    
    @Provider
    public static class MarshallerResolver implements ContextResolver<Marshaller> {
        private JAXBContext context;
        private int invoked;
        public MarshallerResolver() {
            try {
                this.context = JAXBContext.newInstance(JAXBBean.class);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        public Marshaller getContext(Class<?> c) {
            if (JAXBBean.class == c) {
                invoked++;
                try {
                    return context.createMarshaller();
                } catch (JAXBException ex) {
                    throw new RuntimeException(ex);
                }
            } else 
                return null;
        }
        
        public int invoked() {
            return invoked;
        }
    }
    
    @Provider
    public static class UnmarshallerResolver implements ContextResolver<Unmarshaller> {
        private JAXBContext context;
        private int invoked;
        public UnmarshallerResolver() {
            try {
                this.context = JAXBContext.newInstance(JAXBBean.class);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        public Unmarshaller getContext(Class<?> c) {
            if (JAXBBean.class == c) {
                invoked++;
                try {
                    return context.createUnmarshaller();
                } catch (JAXBException ex) {
                    throw new RuntimeException(ex);
                }
            } else 
                return null;
        }
        
        public int invoked() {
            return invoked;
        }
    }
    
    @Path("/")
    public static class JAXBBeanResource {
        @POST
        public JAXBBean get(JAXBBean b) {
            return b;
        }
    }
    
    public void testJAXBContext() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResource.class);
        rc.getSingletons().add(cr);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.type("application/xml").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(2, cr.invoked());
    }        
    
    public void testUnMarshaller() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        MarshallerResolver mr = new MarshallerResolver();
        UnmarshallerResolver umr = new UnmarshallerResolver();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResource.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(mr);
        rc.getSingletons().add(umr);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.type("application/xml").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(1, mr.invoked());
        assertEquals(1, umr.invoked());
    }        
    
    @Provider
    @Produces("application/xml")
    public static class JAXBContextResolverApp extends JAXBContextResolver { }
    
    @Provider
    @Produces("application/xml")
    public static class MarshallerResolverApp extends MarshallerResolver { }
    
    @Provider
    @Produces("application/xml")
    public static class UnmarshallerResolverApp extends UnmarshallerResolver { }
    
    @Path("/")
    @Consumes("application/xml")
    @Produces("application/xml")
    public static class JAXBBeanResourceApp extends JAXBBeanResource { }
        
    public void testJAXBContextApp() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        JAXBContextResolverApp crApp = new JAXBContextResolverApp();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceApp.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(crApp);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.type("application/xml").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(2, crApp.invoked());
    }

    public void testUnMarshallerApp() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        MarshallerResolver mr = new MarshallerResolver();
        UnmarshallerResolver umr = new UnmarshallerResolver();
        MarshallerResolverApp mrApp = new MarshallerResolverApp();
        UnmarshallerResolverApp umrApp = new UnmarshallerResolverApp();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceApp.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(mr);
        rc.getSingletons().add(umr);
        rc.getSingletons().add(mrApp);
        rc.getSingletons().add(umrApp);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.type("application/xml").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, mr.invoked());
        assertEquals(0, umr.invoked());
        assertEquals(1, mrApp.invoked());
        assertEquals(1, umrApp.invoked());
        
        assertEquals("foo", r.type("application/xml;charset=UTF-8").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, mr.invoked());
        assertEquals(0, umr.invoked());
        assertEquals(2, mrApp.invoked());
        assertEquals(2, umrApp.invoked());
    }        
    
    
    
    @Provider
    @Produces("text/xml")
    public static class JAXBContextResolverText extends JAXBContextResolver { }
    
    @Provider
    @Produces("text/xml")
    public static class MarshallerResolverText extends MarshallerResolver { }
    
    @Provider
    @Produces("text/xml")
    public static class UnmarshallerResolverText extends UnmarshallerResolver { }
    
    @Path("/")
    @Consumes("text/xml")
    @Produces("text/xml")
    public static class JAXBBeanResourceText extends JAXBBeanResource { }
        
    public void testJAXBContextText() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        JAXBContextResolverText crText = new JAXBContextResolverText();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceText.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(crText);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.type("text/xml").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(2, crText.invoked());
    }    
    
    public void testUnMarshallerText() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        MarshallerResolver mr = new MarshallerResolver();
        UnmarshallerResolver umr = new UnmarshallerResolver();
        MarshallerResolverText mrText = new MarshallerResolverText();
        UnmarshallerResolverText umrText = new UnmarshallerResolverText();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceText.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(mr);
        rc.getSingletons().add(umr);
        rc.getSingletons().add(mrText);
        rc.getSingletons().add(umrText);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.type("text/xml").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, mr.invoked());
        assertEquals(0, umr.invoked());
        assertEquals(1, mrText.invoked());
        assertEquals(1, umrText.invoked());
    }
    
    
    @Provider
    @Produces("text/foo+xml")
    public static class MarshallerResolverFoo extends MarshallerResolver { }
    
    @Provider
    @Produces("text/foo+xml")
    public static class UnmarshallerResolverFoo extends UnmarshallerResolver { }
    
    @Path("/")
    @Consumes("text/foo+xml")
    @Produces("text/foo+xml")
    public static class JAXBBeanResourceFoo extends JAXBBeanResource { }
    
    public void testUnMarshallerFoo() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        MarshallerResolver mr = new MarshallerResolver();
        UnmarshallerResolver umr = new UnmarshallerResolver();
        MarshallerResolverFoo mrFoo = new MarshallerResolverFoo();
        UnmarshallerResolverFoo umrFoo = new UnmarshallerResolverFoo();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceFoo.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(mr);
        rc.getSingletons().add(umr);
        rc.getSingletons().add(mrFoo);
        rc.getSingletons().add(umrFoo);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.type("text/foo+xml").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, mr.invoked());
        assertEquals(0, umr.invoked());
        assertEquals(1, mrFoo.invoked());
        assertEquals(1, umrFoo.invoked());
        
        assertEquals("foo", r.type("text/foo+xml;charset=UTF-8").post(JAXBBean.class, new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, mr.invoked());
        assertEquals(0, umr.invoked());
        assertEquals(2, mrFoo.invoked());
        assertEquals(2, umrFoo.invoked());
    }
    
    @Path("/")
    public static class JAXBBeanResourceAll {
        @POST
        @Consumes("application/foo+xml")
        @Produces("application/foo+xml")
        public JAXBBean get(JAXBBean b) {
            return b;
        }
        
        @POST
        @Consumes("application/xml")
        @Produces("application/xml")
        public JAXBBean getApp(JAXBBean b) {
            return b;
        }
        
        @POST
        @Consumes("text/xml")
        @Produces("text/xml")
        public JAXBBean getText(JAXBBean b) {
            return b;
        }
    }
    
    public void testJAXBContextAll() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        JAXBContextResolverApp crApp = new JAXBContextResolverApp();
        JAXBContextResolverText crText = new JAXBContextResolverText();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceAll.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(crApp);
        rc.getSingletons().add(crText);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        
        assertEquals("foo", r.type("application/foo+xml").post(JAXBBean.class,
                new JAXBBean("foo")).value);
        assertEquals(2, cr.invoked());
        assertEquals(0, crApp.invoked());
        assertEquals(0, crText.invoked());
        
        assertEquals("foo", r.type("application/xml").post(JAXBBean.class, 
                new JAXBBean("foo")).value);
        assertEquals(2, cr.invoked());
        assertEquals(2, crApp.invoked());
        assertEquals(0, crText.invoked());
        
        assertEquals("foo", r.type("text/xml").post(JAXBBean.class, 
                new JAXBBean("foo")).value);
        assertEquals(2, cr.invoked());
        assertEquals(2, crApp.invoked());
        assertEquals(2, crText.invoked());
    }    
    
    public void testUnMarshallerAll() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        JAXBContextResolverApp crApp = new JAXBContextResolverApp();
        JAXBContextResolverText crText = new JAXBContextResolverText();        
        MarshallerResolver mr = new MarshallerResolver();
        UnmarshallerResolver umr = new UnmarshallerResolver();
        MarshallerResolverApp mrApp = new MarshallerResolverApp();
        UnmarshallerResolverApp umrApp = new UnmarshallerResolverApp();
        MarshallerResolverText mrText = new MarshallerResolverText();
        UnmarshallerResolverText umrText = new UnmarshallerResolverText();        
        
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceAll.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(crApp);
        rc.getSingletons().add(crText);
        rc.getSingletons().add(mr);
        rc.getSingletons().add(umr);
        rc.getSingletons().add(mrApp);
        rc.getSingletons().add(umrApp);
        rc.getSingletons().add(mrText);
        rc.getSingletons().add(umrText);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        
        assertEquals("foo", r.type("application/foo+xml").post(JAXBBean.class,
                new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, crApp.invoked());
        assertEquals(0, crText.invoked());
        assertEquals(1, mr.invoked());
        assertEquals(1, umr.invoked());
        assertEquals(0, mrApp.invoked());
        assertEquals(0, umrApp.invoked());
        assertEquals(0, mrText.invoked());
        assertEquals(0, umrText.invoked());
        
        assertEquals("foo", r.type("application/xml").post(JAXBBean.class, 
                new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, crApp.invoked());
        assertEquals(0, crText.invoked());
        assertEquals(1, mr.invoked());
        assertEquals(1, umr.invoked());
        assertEquals(1, mrApp.invoked());
        assertEquals(1, umrApp.invoked());
        assertEquals(0, mrText.invoked());
        assertEquals(0, umrText.invoked());
        
        assertEquals("foo", r.type("text/xml").post(JAXBBean.class, 
                new JAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, crApp.invoked());
        assertEquals(0, crText.invoked());
        assertEquals(1, mr.invoked());
        assertEquals(1, umr.invoked());
        assertEquals(1, mrApp.invoked());
        assertEquals(1, umrApp.invoked());
        assertEquals(1, mrText.invoked());
        assertEquals(1, umrText.invoked());
    }    
    
    @XmlRootElement
    public static class OtherJAXBBean {

        public String value; 
        public OtherJAXBBean() {}
        public OtherJAXBBean(String str) {
            value = str;
        }

        public boolean equals(Object o) {
            if (!(o instanceof JAXBBean)) 
                return false;
            return ((JAXBBean) o).value.equals(value);
        }

        public String toString() {
            return "JAXBClass: "+value;
        }
    }

    @Path("/")
    public static class JAXBBeanResourceAllOtherJAXBBean {
        @POST
        @Consumes("application/foo+xml")
        @Produces("application/foo+xml")
        public OtherJAXBBean get(OtherJAXBBean b) {
            return b;
        }
        
        @POST
        @Consumes("application/xml")
        @Produces("application/xml")
        public OtherJAXBBean getApp(OtherJAXBBean b) {
            return b;
        }
        
        @POST
        @Consumes("text/xml")
        @Produces("text/xml")
        public OtherJAXBBean getText(OtherJAXBBean b) {
            return b;
        }
    }
    
    public void testJAXBContextAllWithOtherJAXBBean() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        JAXBContextResolverApp crApp = new JAXBContextResolverApp();
        JAXBContextResolverText crText = new JAXBContextResolverText();
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResourceAllOtherJAXBBean.class);
        rc.getSingletons().add(cr);
        rc.getSingletons().add(crApp);
        rc.getSingletons().add(crText);
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        
        assertEquals("foo", r.type("application/foo+xml").post(OtherJAXBBean.class,
                new OtherJAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, crApp.invoked());
        assertEquals(0, crText.invoked());
        
        assertEquals("foo", r.type("application/xml").post(OtherJAXBBean.class, 
                new OtherJAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, crApp.invoked());
        assertEquals(0, crText.invoked());
        
        assertEquals("foo", r.type("text/xml").post(OtherJAXBBean.class, 
                new OtherJAXBBean("foo")).value);
        assertEquals(0, cr.invoked());
        assertEquals(0, crApp.invoked());
        assertEquals(0, crText.invoked());
    }    
}