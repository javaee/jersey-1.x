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

import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.spi.monitoring.DispatchingListener;
import com.sun.jersey.spi.monitoring.DispatchingListenerAdapter;
import com.sun.jersey.spi.monitoring.RequestListener;
import com.sun.jersey.spi.monitoring.RequestListenerAdapter;
import com.sun.jersey.spi.monitoring.ResponseListener;
import com.sun.jersey.spi.monitoring.ResponseListenerAdapter;
import org.glassfish.gmbal.ManagedObjectManagerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * @author pavel.bucek@oracle.com
 */
@Provider
public final class MonitoringAdapter implements RequestListenerAdapter, DispatchingListenerAdapter, ResponseListenerAdapter {

    /**
     * Application name which will be used to register JMX Bean.
     *
     * Value should be String or should produce application name
     * when toString() method is called. Default value is "context-root".
     */
    public static final String PROPERTY_MONITORING_APP_NAME =
            "com.sun.jersey.monitoring.app.name";

    private final MonitoringListener monitoringListener;

    public  MonitoringAdapter(@Context FeaturesAndProperties featuresAndProperties) {
        JMXRegistrator jmxRegistrator;

        if(featuresAndProperties.getProperties().containsKey(PROPERTY_MONITORING_APP_NAME)) {
            jmxRegistrator = new JMXRegistrator(featuresAndProperties.getProperty(PROPERTY_MONITORING_APP_NAME).toString());
        } else {
            jmxRegistrator = new JMXRegistrator("context-root");
        }

        final JerseyJMXGlobalBean jerseyJMXBean = new JerseyJMXGlobalBean(jmxRegistrator);
        jmxRegistrator.registerJerseyJMXGlobalBean(jerseyJMXBean);

        this.monitoringListener = new MonitoringListener(jerseyJMXBean);
    }


    @Override
    public RequestListener adapt(RequestListener requestListener) {
        monitoringListener.setRequestListener(requestListener);
        return monitoringListener;
    }

    @Override
    public DispatchingListener adapt(DispatchingListener dispatchingListener) {
        monitoringListener.setDispatchingListener(dispatchingListener);
        return monitoringListener;
    }

    @Override
    public ResponseListener adapt(ResponseListener responseListener) {
        monitoringListener.setResponseListener(responseListener);
        return monitoringListener;
    }

}
