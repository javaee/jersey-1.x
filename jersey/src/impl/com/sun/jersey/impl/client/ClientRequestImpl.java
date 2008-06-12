/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.jersey.impl.client;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.impl.container.OutBoundHeaders;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

public final class ClientRequestImpl extends ClientRequest {
    private final URI uri;
    
    private final String method;
    
    private final Object entity;
    
    private final MultivaluedMap<String, Object> metadata;

    public ClientRequestImpl(URI uri, String method) {
        this(uri, method, null, null);
    }
    
    public ClientRequestImpl(URI uri, String method, Object entity) {
        this(uri, method, entity, null);
    }
    
    public ClientRequestImpl(URI uri, String method, 
            Object entity, MultivaluedMap<String, Object> metadata) {
        this.uri = uri;
        this.method = method;
        this.entity = entity;
        this.metadata = (metadata != null) ? metadata : new OutBoundHeaders();
    }

    public URI getURI() {
        return uri;
    }

    public String getMethod() {
        return method;
    }
    
    public Object getEntity() {
        return entity;
    }

    public MultivaluedMap<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public ClientRequest clone() {
        return new ClientRequestImpl(this.uri, this.method, 
                this.entity, clone(this.metadata));
    }
    
    private static MultivaluedMap<String, Object> clone(MultivaluedMap<String, Object> md) {
        MultivaluedMap<String, Object> clone = new OutBoundHeaders();
        for (Map.Entry<String, List<Object>> e : md.entrySet()) {
            clone.put(e.getKey(), new ArrayList<Object>(e.getValue()));
        }
        return clone;
    }
}
