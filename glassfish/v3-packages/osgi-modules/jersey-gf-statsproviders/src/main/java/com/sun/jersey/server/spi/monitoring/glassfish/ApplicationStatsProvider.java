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

package com.sun.jersey.server.spi.monitoring.glassfish;

import java.util.HashMap;
import java.util.Map;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.statistics.annotations.Reset;

/**
 *
 * @author pavel.bucek@sun.com
 */

public class ApplicationStatsProvider {

    private final Map<String, ResourceStatsProvider> resourceStatsProviders;

    private final String applicationName;

    @Reset
    public void reset() {
    }

    public ApplicationStatsProvider(String applicationName) {
        resourceStatsProviders = new HashMap<String, ResourceStatsProvider>();
        this.applicationName = applicationName;
    }

    private ResourceStatsProvider getResourceStatsProvider(String resourceName) {

        synchronized (resourceStatsProviders) {

            if (resourceStatsProviders.containsKey(resourceName)) {
                return resourceStatsProviders.get(resourceName);
            } else {
                ResourceStatsProvider rsp = new ResourceStatsProvider(resourceName);

                StatsProviderManager.register(ContainerMonitoring.JERSEY,
                        PluginPoint.SERVER, "applications/" + applicationName + "/jersey/resources/resource-" + resourceStatsProviders.size(),
                        rsp);

                resourceStatsProviders.put(resourceName, rsp);

                return rsp;
            }
        }
        
    }

    public void rootResourceClassHit(String resourceClassName) {
        ResourceStatsProvider rsp = getResourceStatsProvider(resourceClassName);
        rsp.rootResourceHit();
    }


    public void resourceClassHit(String resourceClassName) {
        ResourceStatsProvider rsp = getResourceStatsProvider(resourceClassName);
        rsp.resourceHit();
    }
}
