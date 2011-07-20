/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.monitoring;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.monitoring.events.AbstractEvent;
import com.sun.jersey.monitoring.events.ErrorEvent;
import com.sun.jersey.monitoring.events.MappedExceptionEvent;
import com.sun.jersey.monitoring.events.RequestEvent;
import com.sun.jersey.monitoring.events.ResourceMethodEvent;
import com.sun.jersey.monitoring.events.ResponseEvent;
import com.sun.jersey.monitoring.events.SubResourceEvent;
import com.sun.jersey.monitoring.events.SubResourceLocatorEvent;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.monitoring.DispatchingListener;
import com.sun.jersey.spi.monitoring.RequestListener;
import com.sun.jersey.spi.monitoring.ResponseListener;

import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;

/**
 * @author pavel.bucek@oracle.com
 */
public final class MonitoringListener implements RequestListener, DispatchingListener, ResponseListener {

    private RequestListener requestListener;
    private DispatchingListener dispatchingListener;
    private ResponseListener responseListener;

    private final JerseyJMXGlobalBean jerseyJMXBean;

    MonitoringListener(JerseyJMXGlobalBean jerseyJMXBean) {
        this.jerseyJMXBean = jerseyJMXBean;
    }


    @Override
    public void onRequest(long id, ContainerRequest request) {
        jerseyJMXBean.getContext().put(new Long(id), new ArrayList<AbstractEvent>());
        jerseyJMXBean.getContext().get(new Long(id)).add(new RequestEvent(request));

        requestListener.onRequest(id, request);
    }


    @Override
    public void onSubResourceLocator(long id, AbstractSubResourceLocator locator) {
        jerseyJMXBean.getContext().get(new Long(id)).add(new SubResourceLocatorEvent(locator));

        dispatchingListener.onSubResourceLocator(id, locator);
    }

    @Override
    public void onSubResource(long id, Class subResource) {
        jerseyJMXBean.getContext().get(new Long(id)).add(new SubResourceEvent(subResource));

        dispatchingListener.onSubResource(id, subResource);
    }

    @Override
    public void onResourceMethod(long id, AbstractResourceMethod method) {
        jerseyJMXBean.getContext().get(new Long(id)).add(new ResourceMethodEvent(method));

        dispatchingListener.onResourceMethod(id, method);
    }

    @Override
    public void onError(long id, Throwable ex) {
        jerseyJMXBean.getContext().get(new Long(id)).add(new ErrorEvent(ex));

        responseListener.onError(id, ex);
    }

    @Override
    public void onResponse(long id, ContainerResponse response) {
        jerseyJMXBean.getContext().get(new Long(id)).add(new ResponseEvent(response));

        for(AbstractEvent event : jerseyJMXBean.getContext().get(new Long(id)))
            event.process(jerseyJMXBean);

        jerseyJMXBean.getContext().get(new Long(id)).clear();

        responseListener.onResponse(id, response);
    }

    @Override
    public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
        jerseyJMXBean.getContext().get(new Long(id)).add(new MappedExceptionEvent(exception, mapper));

        responseListener.onMappedException(id, exception, mapper);
    }



    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public void setDispatchingListener(DispatchingListener dispatchingListener) {
        this.dispatchingListener = dispatchingListener;
    }

    public void setResponseListener(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }
}
