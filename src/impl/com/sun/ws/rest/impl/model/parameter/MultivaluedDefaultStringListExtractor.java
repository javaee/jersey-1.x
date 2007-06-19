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

package com.sun.ws.rest.impl.model.parameter;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MultivaluedDefaultStringListExtractor implements MultivaluedParameterExtractor {
    final String parameter;
    final String defaultValue;

    public MultivaluedDefaultStringListExtractor(String parameter, String defaultValue) {
        this.parameter = parameter;
        this.defaultValue = defaultValue;
    }

    public Object extract(MultivaluedMap<String, String> parameters) {
        List<String> stringList = parameters.get(parameter);
        if (stringList != null) {
            List<String> copy = new ArrayList<String>();
            copy.addAll(stringList);
            return copy;
        } else if (defaultValue != null) {
            List<String> l = new ArrayList<String>();
            l.add(defaultValue);
            return l;
        }

        return null;
    }
}
