/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.impl.http.header;

import com.sun.jersey.core.header.MediaTypes;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ConsumeProduceTest extends TestCase {
    
    public ConsumeProduceTest(String testName) {
        super(testName);
    }
    
    @Consumes({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ConsumesClass {
    }
    
    @Produces({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ProducesClass {
    }
    
    /** Creates a new instance of ConsumeProduceTest */
    public ConsumeProduceTest() {
    }
    
    public void testConsumes() {
        Consumes c = ConsumesClass.class.getAnnotation(Consumes.class);
        List<MediaType> l = MediaTypes.createMediaTypes(c);
        checkMediaTypes(l);
    }
    
    public void testProduces() {
        Produces p = ProducesClass.class.getAnnotation(Produces.class);
        List<MediaType> l = MediaTypes.createMediaTypes(p);
        checkMediaTypes(l);
    }
    
    @Consumes("*/*, a/*, b/*, a/b, c/d")
    class ConsumesStringClass {
    }

    @Produces("*/*, a/*, b/*, a/b, c/d")
    class ProducesStringClass {
    }

    public void testConsumesString() {
        Consumes c = ConsumesStringClass.class.getAnnotation(Consumes.class);
        List<MediaType> l = MediaTypes.createMediaTypes(c);
        checkMediaTypes(l);
    }

    public void testProducesString() {
        Produces p = ProducesStringClass.class.getAnnotation(Produces.class);
        List<MediaType> l = MediaTypes.createMediaTypes(p);
        checkMediaTypes(l);
    }

    @Consumes({"*/*, a/*", "b/*, a/b", "c/d"})
    class ConsumesStringsClass {
    }

    @Produces({"*/*, a/*", "b/*, a/b", "c/d"})
    class ProducesStringsClass {
    }


    public void testConsumesStrings() {
        Consumes c = ConsumesStringsClass.class.getAnnotation(Consumes.class);
        List<MediaType> l = MediaTypes.createMediaTypes(c);
        checkMediaTypes(l);
    }

    public void testProducesStrings() {
        Produces p = ProducesStringsClass.class.getAnnotation(Produces.class);
        List<MediaType> l = MediaTypes.createMediaTypes(p);
        checkMediaTypes(l);
    }

    void checkMediaTypes(List<MediaType> l) {
        assertEquals(5, l.size());
        assertEquals("a", l.get(0).getType());
        assertEquals("b", l.get(0).getSubtype());
        assertEquals("c", l.get(1).getType());
        assertEquals("d", l.get(1).getSubtype());
        assertEquals("a", l.get(2).getType());
        assertEquals("*", l.get(2).getSubtype());
        assertEquals("b", l.get(3).getType());
        assertEquals("*", l.get(3).getSubtype());
        assertEquals("*", l.get(4).getType());
        assertEquals("*", l.get(4).getSubtype());
    }
}
