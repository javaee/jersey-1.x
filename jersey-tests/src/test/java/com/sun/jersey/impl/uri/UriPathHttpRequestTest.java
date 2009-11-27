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

package com.sun.jersey.impl.uri;

import com.sun.jersey.impl.*;
import com.sun.jersey.server.impl.application.WebApplicationContext;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.ContainerRequest;
import javax.ws.rs.core.UriInfo;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriPathHttpRequestTest extends TestCase {
    
    public UriPathHttpRequestTest(String testName) {
        super(testName);
    }
    
    public void testGeneral() throws Exception {
        WebApplicationImpl wai = new WebApplicationImpl();
        ContainerRequest r = new TestHttpRequestContext(wai,
                "GET", null,
                "/context/widgets/10", "/context/");
        UriInfo ui = new WebApplicationContext(wai, r, null);
        assertEquals("widgets/10", ui.getPath());
        assertEquals("widgets/10", ui.getPath(true));
        assertEquals("widgets/10", ui.getPath(false));
    }    
    
    public void testEncoded() throws Exception {
        WebApplicationImpl wai = new WebApplicationImpl();
        ContainerRequest r = new TestHttpRequestContext(wai,
                "GET", null,
                "/context/widgets%20/%2010", "/context/");
        UriInfo ui = new WebApplicationContext(wai, r, null);
        assertEquals("widgets / 10", ui.getPath());
        assertEquals("widgets / 10", ui.getPath(true));
        assertEquals("widgets%20/%2010", ui.getPath(false));
    }    
}
