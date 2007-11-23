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


package com.sun.ws.rest.impl.uri.rules;

import com.sun.ws.rest.impl.uri.PathPattern;
import com.sun.ws.rest.impl.uri.rules.automata.AutomataMatchingUriTemplateRules;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import com.sun.ws.rest.spi.uri.rules.UriRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class UriRulesFactory {
    private UriRulesFactory() {}
    
    public static UriRules<UriRule> create(Map<PathPattern, UriRule> rulesMap) {
        List<PatternRulePair<UriRule>> l = new ArrayList<PatternRulePair<UriRule>>();
        for (Map.Entry<PathPattern, UriRule> e : rulesMap.entrySet())
            l.add(new PatternRulePair<UriRule>(e.getKey(), e.getValue()));

        return create(l);
    }
    
    public static UriRules<UriRule> create(List<PatternRulePair<UriRule>> rules) {
        if (rules.size() < 8) {
            return new LinearMatchingUriTemplateRules<UriRule>(rules);
        } else {
            return new AutomataMatchingUriTemplateRules<UriRule>(rules);
        }
    }
}
