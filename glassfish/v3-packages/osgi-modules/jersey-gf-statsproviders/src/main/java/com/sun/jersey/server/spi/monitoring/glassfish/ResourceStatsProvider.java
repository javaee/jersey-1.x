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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedObject;

/**
 *
 * @author pavel.bucek@sun.com
 */
@AMXMetadata(type="jersey-application-mon", group="monitoring")
@ManagedObject
public class ResourceStatsProvider {

    private ResourceStatistcImpl rootResourceClassHitCount;
    private ResourceStatistcImpl resourceClassHitCount;

    private final String resourceClassName;

    public ResourceStatsProvider(String resourceClassName) {
        this.resourceClassName = resourceClassName;
    }

    @ManagedAttribute(id="hitcount")
    public MapStatsImpl getHitCount() {
        HashMap<String, ResourceStatistcImpl> map = new HashMap<String, ResourceStatistcImpl>();

        if(rootResourceClassHitCount != null)
            map.put("rootresource", rootResourceClassHitCount);
        if(resourceClassHitCount != null)
            map.put("resource", resourceClassHitCount);

        return new MapStatsImpl("hitcount", CountStatisticImpl.UNIT_COUNT, "Hit count for resource class " + resourceClassName, map);
    }

    public void rootResourceHit() {
        if (rootResourceClassHitCount == null)
            rootResourceClassHitCount = new ResourceStatistcImpl("RootResource",
                    StatisticImpl.UNIT_COUNT,
                    "Root resource class hit count for " + resourceClassName,
                    resourceClassName);

        rootResourceClassHitCount.increment();
    }

    public void resourceHit() {
        if (resourceClassHitCount == null)
            resourceClassHitCount = new ResourceStatistcImpl("Resource",
                    StatisticImpl.UNIT_COUNT,
                    "Resource class hit count for " + resourceClassName,
                    resourceClassName);

        resourceClassHitCount.increment();
    }

    @ManagedData
    public class MapStatsImpl extends StatisticImpl implements Stats {

        private final Map<String, ResourceStatistcImpl> values;

        public MapStatsImpl(String name, String unit, String desc, Map<String, ResourceStatistcImpl> values) {
            super(name, unit, desc);
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
        public Statistic[] getStatistics() {
            return values.values().toArray(new ResourceStatistcImpl[values.values().size()]);
        }
    }

    public class ResourceStatistcImpl extends StatisticImpl
            implements CountStatistic, InvocationHandler {

        private long count = 0L;
        private final long initCount;
        private final CountStatistic cs =
                (CountStatistic) Proxy.newProxyInstance(
                CountStatistic.class.getClassLoader(),
                new Class[]{CountStatistic.class},
                this);

        private String resourceClassName;
        private String capilatisedName;

        public ResourceStatistcImpl(long countVal, String name, String unit,
                String desc, long sampleTime, long startTime) {
            super(name, unit, desc, startTime, sampleTime);
            count = countVal;
            initCount = countVal;
        }

        public ResourceStatistcImpl(String name, String unit, String desc) {
            this(0L, name, unit, desc, -1L, System.currentTimeMillis());
        }

        public ResourceStatistcImpl(String name, String unit, String desc, String resourceClassName) {
            this(0L, name.toLowerCase(), unit, desc, -1L, System.currentTimeMillis());

            this.resourceClassName = resourceClassName;
            this.capilatisedName = name;
        }


        public synchronized CountStatistic getStatistic() {
            return cs;
        }

        @Override
        public synchronized Map getStaticAsMap() {
            Map m = super.getStaticAsMap();
            m.put("count", getCount());
            m.put("classname", resourceClassName);
            m.put("name", capilatisedName);
            return m;
        }

        @Override
        public synchronized String toString() {
            return super.toString() + NEWLINE + "Count: " + getCount();
        }

        @Override
        public synchronized long getCount() {
            return count;
        }

        public synchronized void setCount(long countVal) {
            count = countVal;
            sampleTime = System.currentTimeMillis();
        }

        public synchronized void increment() {
            count++;
            sampleTime = System.currentTimeMillis();
        }

        public synchronized void increment(long delta) {
            count = count + delta;
            sampleTime = System.currentTimeMillis();
        }

        public synchronized void decrement() {
            count--;
            sampleTime = System.currentTimeMillis();
        }

        @Override
        public synchronized void reset() {
            super.reset();
            count = initCount;
            sampleTime = -1L;
        }

        // todo: equals implementation
        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            Object result;
            try {
                result = m.invoke(this, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
            } finally {
            }
            return result;
        }
    }
}
