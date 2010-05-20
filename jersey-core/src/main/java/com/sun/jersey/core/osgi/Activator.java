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
package com.sun.jersey.core.osgi;

import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.core.spi.scanning.uri.BundleSchemeScanner;
import com.sun.jersey.core.spi.scanning.uri.UriSchemeScanner;
import com.sun.jersey.impl.SpiMessages;
import com.sun.jersey.spi.service.ServiceConfigurationError;
import com.sun.jersey.spi.service.ServiceFinder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

public class Activator implements BundleActivator, SynchronousBundleListener {

    private static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    private BundleContext bundleContext;

    private ConcurrentMap<Long, Map<String, Callable<List<Class>>>> factories = new ConcurrentHashMap<Long, Map<String, Callable<List<Class>>>>();

    private static final class OsgiServiceFinder<T> extends ServiceFinder.ServiceIteratorProvider<T> {

        static final ServiceFinder.ServiceIteratorProvider defaultIterator = new ServiceFinder.DefaultServiceIteratorProvider();

        @Override
        public Iterator<T> createIterator(final Class<T> serviceClass, final String serviceName, ClassLoader loader, boolean ignoreOnClassNotFound) {
            final List<Class> providerClasses = OsgiLocator.locateAll(serviceName);
            if (!providerClasses.isEmpty()) {
                return new Iterator<T>() {

                    Iterator<Class> it = providerClasses.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public T next() {
                        Class<T> nextClass = it.next();
                        try {
                            return serviceClass.cast(nextClass.newInstance());
                        } catch (Exception ex) {
                            ServiceConfigurationError sce = new ServiceConfigurationError(serviceName + ": "
                                    + SpiMessages.PROVIDER_COULD_NOT_BE_CREATED(nextClass.getName(), serviceClass, ex.getLocalizedMessage()));
                            sce.initCause(ex);
                            throw sce;
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            return defaultIterator.createIterator(serviceClass, serviceName, loader, ignoreOnClassNotFound);
        }

        @Override
        public Iterator<Class<T>> createClassIterator(Class<T> service, String serviceName, ClassLoader loader, boolean ignoreOnClassNotFound) {
            final List<Class> providerClasses = OsgiLocator.locateAll(serviceName);
            if (!providerClasses.isEmpty()) {
                return new Iterator<Class<T>>() {

                    Iterator<Class> it = providerClasses.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public Class<T> next() {
                        return it.next();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            return defaultIterator.createClassIterator(service, serviceName, loader, ignoreOnClassNotFound);
        }
    }


    @Override
    public synchronized void start(final BundleContext bundleContext) throws Exception {
        LOGGER.log(Level.FINE, "Activating Jersey core bundle...");

        this.bundleContext = bundleContext;
        
        setOSGiPackageScannerResourceProvider();
        registerBundleSchemeScanner();
        setOSGiServiceFinderIteratorProvider();

        bundleContext.addBundleListener(this);
        registerExistingBundles();

        LOGGER.log(Level.FINE, "Jersey core bundle activated");
    }


    private void registerExistingBundles() {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.STARTING
                    || bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STOPPING) {
                register(bundle);
            }
        }
    }

    private void setOSGiPackageScannerResourceProvider() {
           PackageNamesScanner.setResourcesProvider(new PackageNamesScanner.ResourcesProvider() {

            @Override
            public Enumeration<URL> getResources(String name, ClassLoader cl) throws IOException {
                List<URL> result = new LinkedList<URL>();
                for (Bundle b : bundleContext.getBundles()) {
                    Enumeration<URL> e = (Enumeration<URL>)b.findEntries(name, "*", false);
                    if (e != null) {
                        result.addAll(Collections.list(e));
                    }
                }
                return Collections.enumeration(result);
            }
        });
    }

    private void setOSGiServiceFinderIteratorProvider() {
        ServiceFinder.setIteratorProvider(new OsgiServiceFinder());
    }

    private void registerBundleSchemeScanner() {
        OsgiLocator.register(UriSchemeScanner.class.getName(), new Callable<List<Class>>(){
            @Override
            public List<Class> call() throws Exception {
                List<Class> result = new LinkedList<Class>();
                result.add(BundleSchemeScanner.class);
                return result;
            }
        });
    }

    @Override
    public synchronized void stop(BundleContext bundleContext) throws Exception {
        LOGGER.log(Level.FINE, "Deactivating Jersey core bundle...");

        bundleContext.removeBundleListener(this);
        while (!factories.isEmpty()) {
            unregister(factories.keySet().iterator().next());
        }
        LOGGER.log(Level.FINE, "Jersey core bundle deactivated");
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "checking bundle " + bundle.getBundleId());
        }
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
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "registering service for key " + entry.getKey() + "with value " + entry.getValue());
                }
                OsgiLocator.register(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void unregister(long bundleId) {
        Map<String, Callable<List<Class>>> map = factories.remove(bundleId);
        if (map != null) {
            for (Map.Entry<String, Callable<List<Class>>> entry : map.entrySet()) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "unregistering service for key " + entry.getKey() + "with value " + entry.getValue());
                }
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
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "creating factories for key: " + factoryId);
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream(), "UTF-8"));
                String factoryClassName;
                List<Class> factoryClasses = new ArrayList<Class>();
                while ((factoryClassName = br.readLine()) != null) {
                    if (factoryClassName.trim().length() == 0) {
                        continue;
                    }
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "factory implementation: " + factoryClassName);
                    }
                    factoryClasses.add(bundle.loadClass(factoryClassName));
                }
                br.close();
                return factoryClasses;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "exception caught while creating factories: " + e);
                throw e;
            } catch (Error e) {
                LOGGER.log(Level.WARNING, "error caught while creating factories: " + e);
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
