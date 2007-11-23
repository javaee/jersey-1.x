/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.http.header;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import com.sun.ws.rest.impl.model.MimeHelper;
import java.util.List;
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
    
    @ConsumeMime({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ConsumeMimeClass {
    }
    
    @ProduceMime({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ProduceMimeClass {
    }
    
    /** Creates a new instance of ConsumeProduceTest */
    public ConsumeProduceTest() {
    }
    
    public void testConsumeMime() {
        ConsumeMime c = ConsumeMimeClass.class.getAnnotation(ConsumeMime.class);
        List<MediaType> l = MimeHelper.createMediaTypes(c);
        checkMediaTypes(l);
    }
    
    public void testProduceMime() {
        ProduceMime p = ProduceMimeClass.class.getAnnotation(ProduceMime.class);
        List<MediaType> l = MimeHelper.createMediaTypes(p);
        checkMediaTypes(l);
    }
    
    public void checkMediaTypes(List<MediaType> l) {
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
