/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.core.spi.factory;

import com.sun.jersey.core.util.KeyComparatorHashMap;
import com.sun.jersey.core.util.StringIgnoreCaseKeyComparator;
import java.util.Collections;
import java.util.Map;

/**
 * HTTP header constants for use with {@link ResponseBuilderImpl} and
 * {@link ResponseImpl}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResponseBuilderHeaders {
    public static final int CACHE_CONTROL     = 0;

    public static final int CONTENT_LANGUAGE  = 1;

    public static final int CONTENT_LOCATION  = 2;

    public static final int CONTENT_TYPE      = 3;

    public static final int ETAG              = 4;

    public static final int LAST_MODIFIED     = 5;

    public static final int LOCATION          = 6;


    private static final Map<String, Integer> HEADER_MAP = createHeaderMap();

    private static final String[] HEADER_ARRAY = createHeaderArray();

    private static Map<String, Integer> createHeaderMap() {
        Map<String, Integer> m = new KeyComparatorHashMap<String, Integer>(
                StringIgnoreCaseKeyComparator.SINGLETON);

        m.put("Cache-Control", CACHE_CONTROL);
        m.put("Content-Language", CONTENT_LANGUAGE);
        m.put("Content-Location", CONTENT_LOCATION);
        m.put("Content-Type", CONTENT_TYPE);
        m.put("ETag", ETAG);
        m.put("Last-Modified", LAST_MODIFIED);
        m.put("Location", LOCATION);

        return Collections.unmodifiableMap(m);
    }

    private static String[] createHeaderArray() {
        Map<String, Integer> m = createHeaderMap();

        String[] a = new String[m.size()];
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            a[e.getValue()] = e.getKey();
        }

        return a;
    }

    
    public static int getSize() {
        return HEADER_MAP.size();
    }
    
    public static String getNameFromId(int id) {
        return HEADER_ARRAY[id];
    }

    public static Integer getIdFromName(String name) {
        return HEADER_MAP.get(name);
    }
}
