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
package com.sun.jersey.api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.MediaType;


/**
 * @author Paul.Sandoz@Sun.Com
 */
public class DefaultResourceConfigTest extends AbstractResourceConfigOrderTest {

    public void testClasses() {
        DefaultResourceConfig rc = new DefaultResourceConfig(
                LIST.toArray(new Class<?>[0]));

        assertEquals(LIST, new ArrayList(rc.getClasses()));
    }

    public void testSetClasses() {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>(LIST);
        DefaultResourceConfig rc = new DefaultResourceConfig(classes);

        assertEquals(LIST, new ArrayList(rc.getClasses()));
    }

    public void testSingletons() {
        List l = getList(Arrays.asList(new One(), new Two(), new Three()));
        DefaultResourceConfig rc = new DefaultResourceConfig();
        rc.getSingletons().addAll(l);

        assertEquals(l, new ArrayList(rc.getSingletons()));
    }

    public static class Four {
    }

    public static class Five {
    }

    public static class Six {
    }

    public void testAdd() {
        List rc1Singletons = getList(Arrays.asList(new One(), new Two(), new Three()));
        DefaultResourceConfig rc1 = new DefaultResourceConfig(
                LIST.toArray(new Class<?>[0]));
        rc1.getSingletons().addAll(rc1Singletons);

        List rc2Singletons = getList(Arrays.asList(new Four(), new Five(), new Six()));
        List<Class<?>> rc2Classes = getList(Arrays.asList(Four.class, Five.class, Six.class));
        DefaultResourceConfig rc2 = new DefaultResourceConfig(
                rc2Classes.toArray(new Class<?>[0]));
        rc2.getSingletons().addAll(rc2Singletons);
        rc2.getMediaTypeMappings().put("xml", MediaType.APPLICATION_XML_TYPE);
        rc2.getLanguageMappings().put("en", "en");

        rc2.getExplicitRootResources().put("{test}", new One());

        rc1.add(rc2);


        List<Class<?>> classes = new ArrayList<Class<?>>(rc2Classes);
        classes.addAll(LIST);
        assertEquals(classes, new ArrayList(rc1.getClasses()));

        List singletons = new ArrayList(rc2Singletons);
        singletons.addAll(rc1Singletons);
        assertEquals(singletons, new ArrayList(rc1.getSingletons()));

        assertEquals(MediaType.APPLICATION_XML_TYPE, rc1.getMediaTypeMappings().get("xml"));

        assertEquals("en", rc1.getLanguageMappings().get("en"));

        assertEquals(One.class, rc1.getExplicitRootResources().get("{test}").getClass());
    }
}