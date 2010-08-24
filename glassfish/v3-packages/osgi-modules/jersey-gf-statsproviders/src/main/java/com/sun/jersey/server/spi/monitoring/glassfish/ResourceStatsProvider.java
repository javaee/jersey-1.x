/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.server.spi.monitoring.glassfish;

import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pavel.bucek@sun.com
 */
@AMXMetadata(type="jersey-application-mon", group="monitoring")
@ManagedObject
public class ResourceStatsProvider {

    private ResourceStatisticImpl rootResourceClassHitCount;
    private ResourceStatisticImpl resourceClassHitCount;

    private final String resourceClassName;

    public ResourceStatsProvider(String resourceClassName) {
        this.resourceClassName = resourceClassName;
    }

    @ManagedAttribute(id="hitcount")
    public MapStatsImpl getHitCount() {
        HashMap<String, ResourceStatisticImpl> map = new HashMap<String, ResourceStatisticImpl>();

        if(rootResourceClassHitCount != null)
            map.put("rootresource", rootResourceClassHitCount);
        if(resourceClassHitCount != null)
            map.put("resource", resourceClassHitCount);

        return new MapStatsImpl(map);
    }

    public void rootResourceHit() {
        if (rootResourceClassHitCount == null)
            rootResourceClassHitCount = new ResourceStatisticImpl("RootResourceHitCount",
                    StatisticImpl.UNIT_COUNT,
                    "Root resource class hit count for " + resourceClassName,
                    resourceClassName);

        rootResourceClassHitCount.increment();
    }

    public void resourceHit() {
        if (resourceClassHitCount == null)
            resourceClassHitCount = new ResourceStatisticImpl("ResourceHitCount",
                    StatisticImpl.UNIT_COUNT,
                    "Resource class hit count for " + resourceClassName,
                    resourceClassName);

        resourceClassHitCount.increment();
    }

    @ManagedData
    public class MapStatsImpl implements Stats {

        private final Map<String, ResourceStatisticImpl> values;

        public MapStatsImpl(Map<String, ResourceStatisticImpl> values) {
            this.values = values;
        }

        @Override
        public Statistic getStatistic(String s) {
            return values.get(s);
        }

        @Override
        public String[] getStatisticNames() {
            return values.keySet().toArray(new String[values.keySet().size()]);
        }

        @Override
        @ManagedAttribute
        public ResourceStatisticImpl[] getStatistics() {
            return values.values().toArray(new ResourceStatisticImpl[values.values().size()]);
        }
    }
}
