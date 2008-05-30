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

package com.sun.jersey.impl.container.grizzly;

import com.sun.jersey.spi.container.AbstractContainerResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.jersey.spi.container.MessageBodyContext;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public final class GrizzlyResponseAdaptor extends AbstractContainerResponse {
    
    private final GrizzlyResponse response;
    
    /* package */ GrizzlyResponseAdaptor(GrizzlyResponse response, 
            MessageBodyContext bodyContext, GrizzlyRequestAdaptor requestContext) {
        super(bodyContext, requestContext);
        this.response = response;
    }
    
    
    protected void commitStatusAndHeaders(long contentLength) throws IOException {
        response.setStatus(this.getStatus());
        if (contentLength != -1 && contentLength < Integer.MAX_VALUE) 
            response.setContentLength((int)contentLength);
        for (Map.Entry<String, List<Object>> e : this.getHttpHeaders().entrySet()) {
            String key = e.getKey();
            for (Object value: e.getValue()) {
                response.addHeader(key,value.toString());
            }
        }

        String contentType = response.getHeader("Content-Type");
        if (contentType != null) {
            response.setContentType(contentType);
        }
    }    
    
    /* package */ void commitAll() throws IOException {
        if (isCommitted()) {
            getUnderlyingOutputStream().close();
            return;
        }
        
        writeEntity();
        getUnderlyingOutputStream().close();
    }

    protected OutputStream getUnderlyingOutputStream() throws IOException {
        return response.getOutputStream();
    }
}
