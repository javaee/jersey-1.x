
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

package com.sun.jersey.server.spi.monitoring.glassfish;

import com.sun.jersey.spi.monitoring.AbstractGlassfishMonitoringProvider;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;


/**
 *
 * @author pavel.bucek@sun.com
 */
public class GlassfishMonitoringServiceProvider extends AbstractGlassfishMonitoringProvider {

    @Override
    public void startMonitoring() {
        System.out.println("GlassfishMonitoringServiceProvider - startMonitoring");

        // we need somehow ensure that only one gsp is registered
        // something like:
        // StatsProviderManager.getRegisteredObjects("glassfish", PluginPoint.SERVER, "/jersey/global").count()
        // or we might want to interate over returned object and..:
//
//        boolean found = false;
//        for(Object o : StatsProviderManager.getRegisteredObjects("glassfish", PluginPoint.SERVER, "/jersey/global")) {
//            if(o instanceof GlobalStatsProvider)
//                found = true;
//        }
//
//        if(!found)
//            StatsProviderManager.register("glassfish", PluginPoint.SERVER, "/jersey/global", gsp);


        GlobalStatsProvider gsp = GlobalStatsProvider.getInstance();

        StatsProviderManager.register("web-container", PluginPoint.SERVER, "/jersey/global", gsp);

        // test!

//        gsp.requestStart("app1");
//        gsp.ruleAccept("RootResourceClassesRule", "" , null);
//        gsp.ruleAccept("RightHandPathRule", "", new ResourceClazz1());
//        gsp.ruleAccept("HttpMethodRule", "", new ResourceClazz2());
//        gsp.requestEnd();

        // result:

//$ ./bin/asadmin get --monitor=true "*" | grep jersey
//server.applications.app1.jersey.resources.resourceClassHitCount = {com.sun.jersey.server.spi.monitoring.glassfish.ResourceClazz2=1}
//server.applications.app1.jersey.resources.rootResourceClassHitCount = {com.sun.jersey.server.spi.monitoring.glassfish.ResourceClazz1=1}
//server.jersey.global.applicationList = [app1]

        // looks ok

    }
}

// only for testing
//class ResourceClazz1 {}
//class ResourceClazz2 {}
//class ResourceClazz3 {}

