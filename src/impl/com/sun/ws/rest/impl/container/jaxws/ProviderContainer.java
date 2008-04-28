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

package com.sun.ws.rest.impl.container.jaxws;

import com.sun.jersey.api.container.ContainerException;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.WebApplication;
import java.io.IOException;
import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

/**
 * Container to be used with a JAX-WS <code>javax..xml.ws.Endpoint</code>.
 *
 * @author Doug Kohlert
 */
@BindingType(HTTPBinding.HTTP_BINDING)
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
public class ProviderContainer implements Provider<DataSource>, ContainerListener {
    
    @Resource
    WebServiceContext wsContext;
        
    private WebApplication application;
    
    
    /**
     * Creates a new instance of ProviderContainer.
     * 
     * @param application the web application.
     */
    public ProviderContainer(WebApplication application) {
        this.application = application;
    }
    
    public DataSource invoke(DataSource request) {
        WebApplication _application = application;
        
        DataSource result = null;
        MessageContext msgContext = wsContext.getMessageContext();
        try {
            MessageContextRequestAdaptor requestAdaptor = 
                    new MessageContextRequestAdaptor(
                    _application.getMessageBodyContext(), 
                    request, msgContext);
            MessageContextResponseAdaptor responseAdaptor = 
                    new MessageContextResponseAdaptor(msgContext, 
                    _application.getMessageBodyContext(), 
                    requestAdaptor);

            _application.handleRequest(requestAdaptor, responseAdaptor);
            
            result = responseAdaptor.getResultDataSource();
        } catch(ContainerException e) {
            // Specific error associated with the runtime
            throw new WebServiceException(ImplMessages.NESTED_ERROR(e.getLocalizedMessage()), e);
        } catch (RuntimeException e) {
            // Unexpected error associated with the runtime
            // This is a bug
            throw new WebServiceException(ImplMessages.NESTED_ERROR(e.getLocalizedMessage()), e);
        } catch (IOException e) {
            throw new WebServiceException(ImplMessages.NESTED_ERROR(e.getLocalizedMessage()), e);
        }
        
        return result;
    }

    // ContainerListener
    
    public void onReload() {
        application = application.clone();
    }
}