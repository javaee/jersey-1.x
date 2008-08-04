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

package com.sun.jersey.impl.container.config;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import java.util.HashMap;
import java.util.Map;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class DefaultResourceConfigTest extends TestCase  {
    
    static class TestResourceConfig extends DefaultResourceConfig {
        public TestResourceConfig(Map<String, Object> props) {
            setPropertiesAndFeatures(props);
        }        
    }
    
    public DefaultResourceConfigTest(String testName) {
        super(testName);
    }
    
    public void testFeatures() {
        Map<String, Object> p = new HashMap<String, Object>();
        p.put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, true);
        p.put(ResourceConfig.FEATURE_MATCH_MATRIX_PARAMS, true);
        p.put(ResourceConfig.FEATURE_NORMALIZE_URI, true);
        p.put(ResourceConfig.FEATURE_REDIRECT, true);
        p.put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        p.put("f1", "true");
        p.put("f2", "false");
        p.put("f3", true);
        p.put("f4", false);
        p.put("f5", "TRUE");
        p.put("f6", "FALSE");
        p.put("f7", "TrUe");
        p.put("f8", "FaLsE");
        p.put("f9", "_TrUe");
        p.put("f10", "_FaLsE");
        ResourceConfig rc = new TestResourceConfig(p);

        Map<String, Boolean> fs = rc.getFeatures();
        assertTrue(fs.get(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH));
        assertTrue(fs.get(ResourceConfig.FEATURE_MATCH_MATRIX_PARAMS));
        assertTrue(fs.get(ResourceConfig.FEATURE_NORMALIZE_URI));
        assertTrue(fs.get(ResourceConfig.FEATURE_REDIRECT));
        assertTrue(fs.get(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES));
        assertTrue(fs.get("f1"));
        assertFalse(fs.get("f2"));
        assertTrue(fs.get("f3"));
        assertFalse(fs.get("f4"));
        assertTrue(fs.get("f5"));
        assertFalse(fs.get("f6"));
        assertTrue(fs.get("f7"));
        assertFalse(fs.get("f8"));
        assertNull(fs.get("f9"));
        assertNull(fs.get("f10"));
        
        Map<String, Object> ps = rc.getProperties();
        assertNotNull(ps.get("f1"));
        assertNotNull(ps.get("f2"));
        assertNotNull(ps.get("f3"));
        assertNotNull(ps.get("f4"));
        assertNotNull(ps.get("f5"));
        assertNotNull(ps.get("f6"));
        assertNotNull(ps.get("f7"));
        assertNotNull(ps.get("f8"));
        assertNotNull(ps.get("f9"));
        assertNotNull(ps.get("f10"));        
    }    
}