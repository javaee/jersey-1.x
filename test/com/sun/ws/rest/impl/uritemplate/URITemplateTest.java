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

import com.sun.ws.rest.spi.dispatch.UriTemplateType;
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
        UriTemplateType t = new UriTemplateType(template);
        _testTemplateNames(t.getTemplateVariables(), names);
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
        UriTemplateType t = new UriTemplateType(template);
        Map<String, String> m = new HashMap<String, String>();
        
        System.out.println("TEMPLATE: " + template);
        System.out.println("REGEX: " + t.getTemplateRegex());
        System.out.println("TEMPLATE NAMES: " + t.getTemplateVariables());
        
        boolean isMatch = t.match(uri, m);
        assertTrue(isMatch);
        assertEquals(values.length, t.getTemplateVariables().size());
        
        System.out.println("MAP: " + m);
        
        Iterator<String> names = t.getTemplateVariables().iterator();
        for (String value : values) {
            String mapValue = m.get(names.next());
            assertEquals(value, mapValue);
        }
    }
    
    public void testNullMatching() {
        Map<String, String> m = new HashMap<String, String>();
        
        UriTemplateType t = UriTemplateType.NULL;
        assertEquals(false, t.match("/", m));
        assertEquals(true, t.match(null, m));
        
        t = new UriTemplateType("/{v}");
        assertEquals(false, t.match(null, m));
        assertEquals(true, t.match("/one", m));
    }
    
    public void testNullOrder() {
        List<UriTemplateType> l = new ArrayList<UriTemplateType>();
        
        l.add(UriTemplateType.NULL);
        l.add(new UriTemplateType("/{a}"));
        l.add(new UriTemplateType("/{a}/{b}"));
        l.add(new UriTemplateType("/{a}/one/{b}"));
        
        Collections.sort(l, UriTemplateType.COMPARATOR);
        
        UriTemplateType t = l.get(l.size() - 1);
        assertEquals(UriTemplateType.NULL, t);
    }
    
    public void testSubstitutionArray() {
        _testSubstitutionArray("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "fred", "barney");
        _testSubstitutionArray("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "fred");
        _testSubstitutionArray("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=&option=fred",
                "https", "this-is-spinal-tap", "", "fred");
        _testSubstitutionArray("http://example.org/{a~b}",
                "http://example.org/none%20of%20the%20above",
                "none%20of%20the%20above");
        _testSubstitutionArray("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "quote=to+bo+or+not+to+be");
        _testSubstitutionArray("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "cheeseburger");
        _testSubstitutionArray("http://example.com/{q}",
                "http://example.com/hullo#world",
                "hullo#world");
        _testSubstitutionArray("http://example.com/{e}/",
                "http://example.com//",
                "");
    }
    
    void _testSubstitutionArray(String template, String uri, String... values) {
        UriTemplateType t = new UriTemplateType(template);
        
        assertEquals(uri, t.createURI(values));
    }
    
    public void testSubstitutionMap() {
        _testSubstitutionMap("http://example.org/{a}/{b}/",
                "http://example.org/fred/barney/",
                "a", "fred",
                "b","barney");
        _testSubstitutionMap("http://example.org/page1#{a}",
                "http://example.org/page1#fred",
                "a", "fred");
        _testSubstitutionMap("{scheme}://{20}.example.org?date={wilma}&option={a}",
                "https://this-is-spinal-tap.example.org?date=&option=fred",
                "scheme", "https", 
                "20", "this-is-spinal-tap", 
                "wilma", "", 
                "a", "fred");
        _testSubstitutionMap("http://example.org/{a~b}",
                "http://example.org/none%20of%20the%20above",
                "a~b", "none%20of%20the%20above");
        _testSubstitutionMap("http://example.org?{p}",
                "http://example.org?quote=to+bo+or+not+to+be",
                "p", "quote=to+bo+or+not+to+be");
        _testSubstitutionMap("http://example.com/order/{c}/{c}/{c}/",
                "http://example.com/order/cheeseburger/cheeseburger/cheeseburger/",
                "c", "cheeseburger");
        _testSubstitutionMap("http://example.com/{q}",
                "http://example.com/hullo#world",
                "q", "hullo#world");
        _testSubstitutionMap("http://example.com/{e}/",
                "http://example.com//",
                "e", "");
    }
    
    void _testSubstitutionMap(String template, String uri, String... variablesAndvalues) {
        UriTemplateType t = new UriTemplateType(template);
        
        Map<String, String> variableMap = new HashMap<String, String>();
        for (int i = 0; i < variablesAndvalues.length; i+=2)
            variableMap.put(variablesAndvalues[i], variablesAndvalues[i+1]);
        
        System.out.println(t.createURI(variableMap));
    } 
}
