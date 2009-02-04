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

import com.sun.grizzly.http.SelectorThread;
import junit.framework.TestCase;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.samples.jsonp.config.JAXBContextResolver;
import com.sun.jersey.samples.jsonp.jaxb.ChangeRecordBean;
import java.util.List;
/**
 *
 * @author japod
 */
public class MainTest extends TestCase {
    
    private SelectorThread threadSelector;
    
    private WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        threadSelector = Main.startServer();

        ClientConfig cc = new DefaultClientConfig();
        // use the following jaxb context resolver
        cc.getClasses().add(JAXBContextResolver.class);
        Client c = Client.create(cc);
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        threadSelector.stopEndpoint();
    }

    /**
     * Test checks that the application.wadl is reachable.
     */
    public void testApplicationWadl() {
        String applicationWadl = r.path("application.wadl").get(String.class);
        assertTrue("Something wrong. Returned wadl length is not > 0",
                applicationWadl.length() > 0);
    }

    /**
     * Test check GET on the "changes" resource in "application/json" format.
     */
    public void testGetOnChangesJSONFormat() {
        GenericType<List<ChangeRecordBean>> genericType =
                new GenericType<List<ChangeRecordBean>>() {};
        // get the initial representation
        List<ChangeRecordBean> changes = r.path("changes").
                accept("application/json").get(genericType);
        // check that there are two changes entries
        assertEquals("Expected number of initial changes not found",
                2, changes.size());
    }

    /**
     * Test check GET on the "changes" resource in "application/xml" format.
     */
    public void testGetOnLatestChangeXMLFormat() {
        ChangeRecordBean lastChange = r.path("changes/latest").
                accept("application/xml").get(ChangeRecordBean.class);
        assertEquals(1, lastChange.linesChanged);
    }

    /**
     * Test check GET on the "changes" resource in "application/javascript" format.
     */
    public void testGetOnLatestChangeJavasriptFormat() {
        String js = r.path("changes").
                accept("application/javascript").get(String.class);
        assertTrue(js.startsWith("callback"));
    }
}