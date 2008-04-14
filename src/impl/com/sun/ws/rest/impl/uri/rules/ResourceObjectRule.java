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

import com.sun.ws.rest.api.uri.UriTemplate;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import com.sun.ws.rest.spi.uri.rules.UriRuleContext;
import java.util.Iterator;

/**
 * The rule for accepting a resource object.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceObjectRule extends BaseRule {

    private final Object resourceObject;
    
    public ResourceObjectRule(UriTemplate template, Object resourceObject) {
        super(template);
        this.resourceObject = resourceObject;
    }
    
    public boolean accept(CharSequence path, Object resource, UriRuleContext context) {
        // Set the template values
        setTemplateValues(context);

        // Match sub-rules on the resource class
        final Iterator<UriRule> matches = context.getRules(resourceObject.getClass()).
                match(path, context.getGroupValues());
        while(matches.hasNext())
            if(matches.next().accept(path, resourceObject, context))
                return true;
        
        return false;
    }
}