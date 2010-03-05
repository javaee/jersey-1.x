/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.servicemix.specs.locator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

public class Activator implements BundleActivator, SynchronousBundleListener {

    private static boolean debug = false;

    private ConcurrentMap<Long, Map<String, Callable<List<Class>>>> factories = new ConcurrentHashMap<Long, Map<String, Callable<List<Class>>>>();

    private BundleContext bundleContext;

    static {
        try {
            String prop = System.getProperty("org.apache.servicemix.specs.debug");
            debug = prop != null && !"false".equals(prop);
        } catch (Throwable t) { }
    }

    /**
     * <p>Output debugging messages.</p>
     *
     * @param msg <code>String</code> to print to <code>stderr</code>.
     */
    protected void debugPrintln(String msg) {
        if (debug) {
            System.err.println("Spec(" + bundleContext.getBundle().getBundleId() + "): " + msg);
        }
    }

    @Override
    public synchronized void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        debugPrintln("activating");
        debugPrintln("adding bundle listener");
        bundleContext.addBundleListener(this);
        debugPrintln("checking existing bundles");
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.STARTING ||
                    bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STOPPING) {
                register(bundle);
            }
        }
        debugPrintln("activated");
    }

    @Override
    public synchronized void stop(BundleContext bundleContext) throws Exception {
        debugPrintln("deactivating");
        bundleContext.removeBundleListener(this);
        while (!factories.isEmpty()) {
            unregister(factories.keySet().iterator().next());
        }
        debugPrintln("deactivated");
        this.bundleContext = null;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.RESOLVED) {
            register(event.getBundle());
        } else if (event.getType() == BundleEvent.UNRESOLVED || event.getType() == BundleEvent.UNINSTALLED) {
            unregister(event.getBundle().getBundleId());
        }
    }

    protected void register(final Bundle bundle) {
        debugPrintln("checking bundle " + bundle.getBundleId());
        Map<String, Callable<List<Class>>> map = factories.get(bundle.getBundleId());
        Enumeration e = bundle.findEntries("META-INF/services/", "*", false);
        if (e != null) {
            while (e.hasMoreElements()) {
                final URL u = (URL) e.nextElement();
                final String url = u.toString();
                if (url.endsWith("/")) {
                    continue;
                }
                final String factoryId = url.substring(url.lastIndexOf("/") + 1);
                if (map == null) {
                    map = new HashMap<String, Callable<List<Class>>>();
                    factories.put(bundle.getBundleId(), map);
                }
                map.put(factoryId, new BundleFactoryLoader(factoryId, u, bundle));
            }
        }
        if (map != null) {
            for (Map.Entry<String, Callable<List<Class>>> entry : map.entrySet()) {
                debugPrintln("registering service for key " + entry.getKey() + "with value " + entry.getValue());
                OsgiLocator.register(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void unregister(long bundleId) {
        Map<String, Callable<List<Class>>> map = factories.remove(bundleId);
        if (map != null) {
            for (Map.Entry<String, Callable<List<Class>>> entry : map.entrySet()) {
                debugPrintln("unregistering service for key " + entry.getKey() + "with value " + entry.getValue());
                OsgiLocator.unregister(entry.getKey(), entry.getValue());
            }
        }
    }

    private class BundleFactoryLoader implements Callable<List<Class>> {
        private final String factoryId;
        private final URL u;
        private final Bundle bundle;

        public BundleFactoryLoader(String factoryId, URL u, Bundle bundle) {
            this.factoryId = factoryId;
            this.u = u;
            this.bundle = bundle;
        }

        @Override
        public List<Class> call() throws Exception {
            try {
                debugPrintln("creating factories for key: " + factoryId);
                BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream(), "UTF-8"));
                String factoryClassName;
                List<Class> factoryClasses = new ArrayList<Class>();
                while ((factoryClassName = br.readLine()) != null) {
                    if (factoryClassName.trim().length() == 0) {
                        continue;
                    }
                    debugPrintln("factory implementation: " + factoryClassName);
                    factoryClasses.add(bundle.loadClass(factoryClassName));
                }
                br.close();
                return factoryClasses;
            } catch (Exception e) {
                debugPrintln("exception caught while creating factories: " + e);
                throw e;
            } catch (Error e) {
                debugPrintln("error caught while creating factories: " + e);
                throw e;
            }
        }

        @Override
        public String toString() {
           return u.toString();
        }

        @Override
        public int hashCode() {
           return u.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BundleFactoryLoader) {
                return u.equals(((BundleFactoryLoader) obj).u);
            } else {
                return false;
            }
        }
    }
}
