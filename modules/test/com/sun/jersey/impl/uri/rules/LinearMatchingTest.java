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

package com.sun.jersey.impl.uri.rules;

import com.sun.jersey.impl.uri.rules.PatternRulePair;
import com.sun.jersey.impl.uri.rules.AtomicMatchingPatterns;
import com.sun.jersey.impl.uri.PathPattern;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class LinearMatchingTest extends AbstractMatchingTester {
    
    public LinearMatchingTest(String testName) {
        super(testName);
    }
    
    private class LinearRulesBuilder extends RulesBuilder {
        protected UriRules<String> _build() {
            List<PatternRulePair<String>> l = new ArrayList<PatternRulePair<String>>();
            for (Map.Entry<PathPattern, String> e : rulesMap.entrySet())
                l.add(new PatternRulePair<String>(e.getKey(), e.getValue()));            
            return new AtomicMatchingPatterns<String>(l);
        }
    }
    
    protected RulesBuilder create() {
        return new LinearRulesBuilder();
    }
}