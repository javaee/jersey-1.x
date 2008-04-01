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

import com.sun.ws.rest.api.model.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public final class ParameterProcessorFactory {
    
    private static final Map<Parameter.Source, ParameterProcessor> S2P_MAP = createMap();

    private static Map<Parameter.Source, ParameterProcessor> createMap() {
        Map<Parameter.Source, ParameterProcessor> map = new HashMap<Parameter.Source, ParameterProcessor>();
        map.put(Parameter.Source.CONTEXT, new HttpContextParameterProcessor());
        map.put(Parameter.Source.HEADER, new HeaderParameterProcessor());
        map.put(Parameter.Source.COOKIE, new CookieParameterProcessor());
        map.put(Parameter.Source.QUERY, new QueryParameterProcessor());
        map.put(Parameter.Source.MATRIX, new MatrixParameterProcessor());
        map.put(Parameter.Source.URI, new UriParameterProcessor());
        return Collections.unmodifiableMap(map);
    }
    
    public static ParameterProcessor createParameterProcessor(Parameter.Source source) {
        if (S2P_MAP.containsKey(source)) {
            return S2P_MAP.get(source);
        } else {
            return null;
        }
    }
}
