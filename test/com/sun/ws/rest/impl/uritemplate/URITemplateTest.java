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

package com.sun.ws.rest.impl.uritemplate;

import com.sun.ws.rest.spi.dispatch.URITemplateType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class URITemplateTest extends TestCase {
    
    public URITemplateTest(String testName) {
        super(testName);
    }
 
    public void testTemplateNames() {
        _testTemplateNames("http://example.org/{a}/{b}/", 
                "a", "b");
        _testTemplateNames("http://example.org/page1#{a}", 
                "a");
        _testTemplateNames("{scheme}://{20}.example.org?date={wilma}&option={a}", 
                "scheme", "20", "wilma", "a");
        _testTemplateNames("http://example.org/{a~b}", 
                "a~b");
        _testTemplateNames("http://example.org?{p}", 
                "p");
        _testTemplateNames("http://example.com/order/{c}/{c}/{c}/", 
                "c", "c", "c");
    }
   
    void _testTemplateNames(String template, String... names) {
        URITemplateType t = new URITemplateType(template);        
        _testTemplateNames(t.getTemplateNames(), names);
    }
    
    void _testTemplateNames(List<String> regexNames, String... names) {
        assertEquals(names.length, regexNames.size());
        
        Iterator<String> i = regexNames.iterator();
        for(String name : names) {
            assertEquals(name, i.next());
        }
    }
    
    public void testMatching() {
       _testMatching("http://example.org/{a}/{b}/", 
                "http://example.org/fred/barney/",
                "fred", "barney");
       _testMatching("http://example.org/page1#{a}", 
                "http://example.org/page1#fred",
                "fred");
       _testMatching("{scheme}://{20}.example.org?date={wilma}&option={a}", 
                "https://this-is-spinal-tap.example.org?date=&option=fred",
                "https", "this-is-spinal-tap", "", "fred");
       _testMatching("http://example.org/{a~b}", 
                "http://example.org/none%20of%20the%20above",
                "none%20of%20the%20above");
       _testMatching("http://example.org?{p}", 
                "http://example.org?quote=to+bo+or+not+to+be",
                "quote=to+bo+or+not+to+be");
       _testMatching("http://example.com/order/{c}/{c}/{c}/", 
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "cheeseburger", "cheeseburger", "cheeseburger");
       _testMatching("http://example.com/{q}", 
                "http://example.com/hullo#world",
                "hullo#world");
       _testMatching("http://example.com/{e}/", 
                "http://example.com//",
                "");
    }
    
    void _testMatching(String template, String uri, String... values) {
        URITemplateType t = new URITemplateType(template);
        Map<String, String> m = new HashMap<String, String>();

        System.out.println("TEMPLATE: " + template);
        System.out.println("REGEX: " + t.getTemplateRegex());
        System.out.println("TEMPLATE NAMES: " + t.getTemplateNames());
        
        boolean isMatch = t.match(uri, m);
        assertTrue(isMatch);
        assertEquals(values.length, t.getTemplateNames().size());
        
        System.out.println("MAP: " + m);
        
        Iterator<String> names = t.getTemplateNames().iterator();
        for (String value : values) {
            String mapValue = m.get(names.next());
            assertEquals(value, mapValue);
        }
    }
    
    public void testNullMatching() {
        Map<String, String> m = new HashMap<String, String>();
        
        URITemplateType t = URITemplateType.NULL;
        assertEquals(false, t.match("/", m));
        assertEquals(true, t.match(null, m));
        
        t = new URITemplateType("/{v}");
        assertEquals(false, t.match(null, m));
        assertEquals(true, t.match("/one", m));
    }
    
    public void testNullOrder() {
        List<URITemplateType> l = new ArrayList<URITemplateType>();
        
        l.add(URITemplateType.NULL);
        l.add(new URITemplateType("/{a}"));
        l.add(new URITemplateType("/{a}/{b}"));
        l.add(new URITemplateType("/{a}/one/{b}"));
        
        Collections.sort(l, URITemplateType.COMPARATOR);
        
        URITemplateType t = l.get(l.size() - 1);
        assertEquals(URITemplateType.NULL, t);
    }
}
