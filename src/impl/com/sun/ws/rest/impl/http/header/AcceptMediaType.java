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

import javax.ws.rs.core.MediaType;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import java.util.Map;

public class AcceptMediaType extends MediaType {
    public static final int MINUMUM_QUALITY = 0;
    
    public static final int MAXIMUM_QUALITY = 1000;
    
    private final int q;

    public AcceptMediaType(String p, String s) {
        super(p, s);
        q = MAXIMUM_QUALITY;
    }
    
    public AcceptMediaType(String p, String s, int q, Map<String, String> parameters) {
        super(p, s, parameters);
        this.q = q;
    }
        
    public int getQ() {
        return q;
    }
    
}
