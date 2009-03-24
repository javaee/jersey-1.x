/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.samples.extendedwadl;

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.util.ApplicationDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Naresh (Srinivas.Bhimisetty@Sun.com)
 */
public class ExtendedWadlWebappTest extends JerseyTest {
    
    public ExtendedWadlWebappTest() throws Exception {
        super();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages",
                "com.sun.jersey.samples.extendedwadl.resources");
        initParams.put("com.sun.jersey.config.property.WadlGeneratorConfig",
                "com.sun.jersey.samples.extendedwadl.SampleWadlGeneratorConfig");
        ApplicationDescriptor appDescriptor = new ApplicationDescriptor();
        appDescriptor.setServletInitParams(initParams);
        appDescriptor.setContextPath("extended-wadl-webapp");
        super.setupTestEnvironment(appDescriptor);
    }
    
    /**
     * Test checks that the WADL generated using the WadlGenerator api doesn't
     * contain the expected text.
     * @throws java.lang.Exception
     */
    @Test
    public void testExtendedWadl() throws Exception {
        String wadl = webResource.path("application.wadl").accept(MediaTypes.WADL).get(String.class);
        Assert.assertTrue("Generated wadl is of null length", wadl.length() > 0);
        Assert.assertTrue("Generated wadl doesn't contain the expected text",
                wadl.contains("This is a paragraph"));
    }

}
