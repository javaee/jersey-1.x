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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.gmbal.ManagedAttribute;


public class ResourceStatisticImpl extends StatisticImpl
            implements CountStatistic, InvocationHandler {

    private long count = 0L;
    private final long initCount;
    private final CountStatistic cs =
            (CountStatistic) Proxy.newProxyInstance(
            CountStatistic.class.getClassLoader(),
            new Class[]{CountStatistic.class},
            this);
    private String resourceClassName;

    public ResourceStatisticImpl(long countVal, String name, String unit,
            String desc, long sampleTime, long startTime) {
        super(name, unit, desc, startTime, sampleTime);

        count = countVal;
        initCount = countVal;
    }

    public ResourceStatisticImpl(String name, String unit, String desc) {
        this(0L, name, unit, desc, -1L, System.currentTimeMillis());
    }

    public ResourceStatisticImpl(String name, String unit, String desc, String resourceClassName) {
        this(0L, name, unit, desc, -1L, System.currentTimeMillis());

        this.resourceClassName = resourceClassName;
    }

    public synchronized CountStatistic getStatistic() {
        return cs;
    }

    @Override
    public synchronized Map getStaticAsMap() {
        Map m = super.getStaticAsMap();
        m.put("count", getCount());
        m.put("classname", resourceClassName);
        return m;
    }

    @Override
    public synchronized String toString() {
        return super.toString() + NEWLINE + "Count: " + getCount();
    }

    @Override
    @ManagedAttribute(id = "count")
    public synchronized long getCount() {
        return count;
    }

    public synchronized void setCount(long countVal) {
        count = countVal;
        sampleTime = System.currentTimeMillis();
    }

    @ManagedAttribute(id = "classname")
    public synchronized String getClassName() {
        return resourceClassName;
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
