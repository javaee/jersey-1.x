/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.spi.container;

import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * An adapting in-bound HTTP response that may override the behaviour of
 * {@link ContainerResponse}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AdaptingContainerResponse extends ContainerResponse {

    /**
     * The adapted container response.
     */
    protected final ContainerResponse acr;

    /**
     * Create the adapting container response.
     *
     * @param acr the container response to adapt.
     */
    protected AdaptingContainerResponse(ContainerResponse acr) {
        super(acr);
        this.acr = acr;
    }

    @Override
    public void write() throws IOException {
        acr.write();
    }

    @Override
    public void reset() {
        acr.reset();
    }

    @Override
    public ContainerRequest getContainerRequest() {
        return acr.getContainerRequest();
    }

    @Override
    public void setContainerRequest(ContainerRequest request) {
        acr.setContainerRequest(request);
    }
    
    @Override
    public ContainerResponseWriter getContainerResponseWriter() {
        return acr.getContainerResponseWriter();
    }
    
    @Override
    public void setContainerResponseWriter(ContainerResponseWriter responseWriter) {
        acr.setContainerResponseWriter(responseWriter);
    }
    
    @Override
    public MessageBodyWorkers getMessageBodyWorkers() {
        return acr.getMessageBodyWorkers();
    }

    @Override
    public Response getResponse() {
        return acr.getResponse();
    }
    
    @Override
    public void setResponse(Response response) {
        acr.setResponse(response);
    }
    
    @Override
    public boolean isResponseSet() {
        return acr.isResponseSet();
    }
    
    @Override
    public int getStatus() {
        return acr.getStatus();
    }
    
    @Override
    public void setStatus(int status) {
        acr.setStatus(status);
    }
    
    @Override
    public Object getEntity() {
        return acr.getEntity();
    }
    
    @Override
    public Type getEntityType() {
        return acr.getEntityType();
    }
    
    @Override
    public void setEntity(Object entity) {
        acr.setEntity(entity);
    }
    
    @Override
    public Annotation[] getAnnotations() {
        return acr.getAnnotations();
    }

    @Override
    public void setAnnotations(Annotation[] annotations) {
        acr.setAnnotations(annotations);
    }
    
    @Override
    public MultivaluedMap<String, Object> getHttpHeaders() {
        return acr.getHttpHeaders();
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return acr.getOutputStream();
    }
    
    @Override
    public boolean isCommitted() {
        return acr.isCommitted();
    }
}