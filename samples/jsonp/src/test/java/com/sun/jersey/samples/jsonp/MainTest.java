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

package com.sun.jersey.samples.jsonp;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.samples.jsonp.config.JAXBContextResolver;
import com.sun.jersey.samples.jsonp.jaxb.ChangeRecordBean;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author japod
 */
public class MainTest extends JerseyTest {

     protected AppDescriptor configure() {
        ClientConfig cc = new DefaultClientConfig();
        // use the following jaxb context resolver
        cc.getClasses().add(JAXBContextResolver.class);
        return new WebAppDescriptor.Builder("com.sun.jersey.samples.jsonp")
                .contextPath("jsonp")
                .clientConfig(cc)
                .build();
    }
    
    /**
     * Test checks that the application.wadl is reachable.
     */
    @Test
    public void testApplicationWadl() {
        WebResource webResouce = resource();
        String applicationWadl = webResouce.path("application.wadl").get(String.class);
        assertTrue("Something wrong. Returned wadl length is not > 0",
                applicationWadl.length() > 0);
    }

    /**
     * Test check GET on the "changes" resource in "application/json" format.
     */
    @Test
    public void testGetOnChangesJSONFormat() {
        WebResource webResouce = resource();
        GenericType<List<ChangeRecordBean>> genericType =
                new GenericType<List<ChangeRecordBean>>() {};
        // get the initial representation
        List<ChangeRecordBean> changes = webResouce.path("changes").
                accept("application/json").get(genericType);
        // check that there are two changes entries
        assertEquals("Expected number of initial changes not found",
                5, changes.size());
    }

    /**
     * Test check GET on the "changes" resource in "application/xml" format.
     */
    @Test
    public void testGetOnLatestChangeXMLFormat() {
        WebResource webResouce = resource();
        ChangeRecordBean lastChange = webResouce.path("changes/latest").
                accept("application/xml").get(ChangeRecordBean.class);
        assertEquals(1, lastChange.linesChanged);
    }

    /**
     * Test check GET on the "changes" resource in "application/javascript" format.
     */
    @Test
    public void testGetOnLatestChangeJavasriptFormat() {
        WebResource webResouce = resource();
        String js = webResouce.path("changes").
                accept("application/x-javascript").get(String.class);
        assertTrue(js.startsWith("callback"));
    }
}
