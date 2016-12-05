/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.core.osgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ext.RuntimeDelegate;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleReference;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.SynchronousBundleListener;

import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.impl.SpiMessages;
import com.sun.jersey.spi.service.ServiceConfigurationError;
import com.sun.jersey.spi.service.ServiceFinder;

/**
 * Utility class to deal with OSGi runtime specific behavior.
 * This is mainly to handle META-INF/services lookup
 * and generic/application class lookup issue in OSGi.
 *
 * When OSGi runtime is detected by the {@link com.sun.jersey.core.reflection.ReflectionHelper} class,
 * an instance of OsgiRegistry is created and associated with given
 * OSGi BundleContext. META-INF/services entries are then being accessed
 * via the OSGi Bundle API as direct ClassLoader#getResource() method invocation
 * does not work in this case within OSGi.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public final class OsgiRegistry implements SynchronousBundleListener {

    private static final String CoreBundleSymbolicNAME = "com.sun.jersey.core";
    private static final Logger LOGGER = Logger.getLogger(OsgiRegistry.class.getName());

    private final BundleContext bundleContext;
    private final Map<Long, Map<String, Callable<List<Class<?>>>>> factories = new HashMap<Long, Map<String, Callable<List<Class<?>>>>>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static OsgiRegistry instance;

    private Map<String, Bundle> classToBundleMapping = new HashMap<String, Bundle>();

    /**
     * Returns an {@code OsgiRegistry} instance. Call this method only if sure that the application is running in OSGi
     * environment, otherwise a call to this method can lead to an {@link ClassNotFoundException}.
     *
     * @return an {@code OsgiRegistry} instance.
     */
    public static synchronized OsgiRegistry getInstance() {
        if (instance == null) {
            final ClassLoader classLoader = ReflectionHelper.class.getClassLoader();
            if (classLoader instanceof BundleReference) {
                final BundleContext context = FrameworkUtil.getBundle(OsgiRegistry.class).getBundleContext();
                if (context != null) { // context could be still null in GlassFish
                    instance = new OsgiRegistry(context);
                    instance.hookUp();
                }
            }
        }
        return instance;
    }

    private final class OsgiServiceFinder<T> extends ServiceFinder.ServiceIteratorProvider<T> {

        final ServiceFinder.ServiceIteratorProvider defaultIterator = new ServiceFinder.DefaultServiceIteratorProvider();

        @Override
        public Iterator<T> createIterator(final Class<T> serviceClass, final String serviceName, ClassLoader loader, boolean ignoreOnClassNotFound) {
            final List<Class<?>> providerClasses = locateAllProviders(serviceName);
            if (!providerClasses.isEmpty()) {
                return new Iterator<T>() {

                    Iterator<Class<?>> it = providerClasses.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public T next() {
                        Class<T> nextClass = (Class<T>) it.next();
                        try {
                            return serviceClass.cast(nextClass.newInstance());
                        } catch (Exception ex) {
                            ServiceConfigurationError sce = new ServiceConfigurationError(serviceName + ": "
                                    + SpiMessages.PROVIDER_COULD_NOT_BE_CREATED(nextClass.getName(), serviceClass,
                                    ex.getLocalizedMessage()));
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
            final List<Class<?>> providerClasses = locateAllProviders(serviceName);
            if (!providerClasses.isEmpty()) {
                return new Iterator<Class<T>>() {

                    Iterator<Class<?>> it = (Iterator<Class<?>>) providerClasses.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public Class<T> next() {
                        return (Class<T>) it.next();
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

    private static class BundleSpiProvidersLoader implements Callable<List<Class<?>>> {

        private final String spi;
        private final URL spiRegistryUrl;
        private final String spiRegistryUrlString;
        private final Bundle bundle;

        BundleSpiProvidersLoader(final String spi, final URL spiRegistryUrl, final Bundle bundle) {
            this.spi = spi;
            this.spiRegistryUrl = spiRegistryUrl;
            this.spiRegistryUrlString = spiRegistryUrl.toExternalForm();
            this.bundle = bundle;
        }

        @Override
        public List<Class<?>> call() throws Exception {
            BufferedReader reader = null;
            try {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Loading providers for SPI: {0}", spi);
                }

                reader = new BufferedReader(new InputStreamReader(spiRegistryUrl.openStream(), "UTF-8"));
                String providerClassName;

                final List<Class<?>> providerClasses = new ArrayList<Class<?>>();
                while ((providerClassName = reader.readLine()) != null) {
                    if (providerClassName.trim().length() == 0) {
                        continue;
                    }
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "SPI provider: {0}", providerClassName);
                    }
                    providerClasses.add(bundle.loadClass(providerClassName));
                }

                return providerClasses;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "exception caught while creating factories: " + e);
                throw e;
            } catch (Error e) {
                LOGGER.log(Level.WARNING, "error caught while creating factories: " + e);
                throw e;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.FINE, "Error closing SPI registry stream:" + spiRegistryUrl, ioe);
                    }
                }
            }
        }

        @Override
        public String toString() {
            return spiRegistryUrlString;
        }

        @Override
        public int hashCode() {
            return spiRegistryUrlString.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BundleSpiProvidersLoader) {
                return spiRegistryUrlString.equals(((BundleSpiProvidersLoader) obj).spiRegistryUrlString);
            } else {
                return false;
            }
        }
    }


    @Override
    public void bundleChanged(BundleEvent event) {

        if (event.getType() == BundleEvent.RESOLVED) {
            register(event.getBundle());
        } else if (event.getType() == BundleEvent.UNRESOLVED || event.getType() == BundleEvent.UNINSTALLED) {

            final Bundle unregisteredBundle = event.getBundle();

            lock.writeLock().lock();
            try {
                factories.remove(unregisteredBundle.getBundleId());
                classToBundleMapping.values().removeAll(Collections.singleton(unregisteredBundle));

                if (unregisteredBundle.getSymbolicName().equals(CoreBundleSymbolicNAME)) {
                    bundleContext.removeBundleListener(this);
                    factories.clear();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private void setOSGiPackageScannerResourceProvider() {
        PackageNamesScanner.setResourcesProvider(new PackageNamesScanner.ResourcesProvider() {

            @Override
            public Enumeration<URL> getResources(String packagePath, ClassLoader classLoader) throws IOException {
                List<URL> result = new LinkedList<URL>();

                for (Bundle bundle : bundleContext.getBundles()) {
                    // Look for resources at the given <packagePath> and at WEB-INF/classes/<packagePath> in case a WAR is
                    // being examined.
                    for (String bundlePackagePath : new String[]{packagePath, "WEB-INF/classes/" + packagePath}) {
                        final Enumeration<URL> enumeration = (Enumeration<URL>) bundle.findEntries(bundlePackagePath, "*", false);

                        if (enumeration != null) {
                            while (enumeration.hasMoreElements()) {
                                final URL url = enumeration.nextElement();
                                final String path = url.getPath();

                                final String className = (packagePath + path.substring(path.lastIndexOf('/'))).
                                        replace('/', '.').replace(".class", "");

                                classToBundleMapping.put(className, bundle);
                                result.add(url);
                            }
                        }
                    }

                    // Now interested only in .jar provided by current bundle.
                    final Enumeration<URL> jars = bundle.findEntries("/", "*.jar", true);
                    if (jars != null) {
                        while (jars.hasMoreElements()) {
                            final URL jar = jars.nextElement();
                            final InputStream inputStream = classLoader.getResourceAsStream(jar.getPath());
                            if (inputStream == null) {
                                LOGGER.config(SpiMessages.OSGI_REGISTRY_ERROR_OPENING_RESOURCE_STREAM(jar));
                                continue;
                            }
                            final JarInputStream jarInputStream;
                            try {
                                jarInputStream = new JarInputStream(inputStream);
                            } catch (IOException ex) {
                                LOGGER.log(Level.CONFIG, SpiMessages.OSGI_REGISTRY_ERROR_PROCESSING_RESOURCE_STREAM(jar), ex);
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    // ignored
                                }
                                continue;
                            }

                            try {
                                JarEntry jarEntry;
                                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                                    final String jarEntryName = jarEntry.getName();

                                    if (jarEntryName.endsWith(".class") && jarEntryName.contains(packagePath)) {
                                        classToBundleMapping.put(jarEntryName.replace(".class", "").replace('/', '.'), bundle);
                                        result.add(bundle.getResource(jarEntryName));
                                    }
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.CONFIG, SpiMessages.OSGI_REGISTRY_ERROR_PROCESSING_RESOURCE_STREAM(jar), ex);
                            } finally {
                                try {
                                    jarInputStream.close();
                                } catch (IOException e) {
                                    // ignored
                                }
                            }
                        }
                    }
                }

                return Collections.enumeration(result);
            }
        });
    }

    /**
     * Get the Class from the class name.
     * <p>
     * The context class loader will be utilized if accessible and non-null.
     * Otherwise the defining class loader of this class will
     * be utilized.
     *
     * @param className the class name.
     * @return the Class, otherwise null if the class cannot be found.
     * @throws ClassNotFoundException if the class cannot be found.
     */
    public Class<?> classForNameWithException(final String className) throws ClassNotFoundException {
        final Bundle bundle = classToBundleMapping.get(className);

        if (bundle == null) {
            throw new ClassNotFoundException(className);
        }
        return bundle.loadClass(className);
    }

    /**
     * Creates a new OsgiRegistry instance bound to a particular OSGi runtime.
     * The only parameter must be an instance of a {@link BundleContext}.
     *
     * @param bundleContext must be a non-null instance of a BundleContext
     */
    private OsgiRegistry(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Will hook up this instance with the OSGi runtime.
     * This is to actually update SPI provider lookup and class loading mechanisms in Jersey
     * to utilize OSGi features.
     */
    private void hookUp() {
        setOSGiPackageScannerResourceProvider();
        setOSGiServiceFinderIteratorProvider();

        bundleContext.addBundleListener(this);
        registerExistingBundles();

        // Register RuntimeDelegate.
        final Bundle jerseyServerBundle = getJerseyServerBundle(bundleContext);
        RuntimeDelegate runtimeDelegate = null;

        try {
            if (jerseyServerBundle == null) {
                LOGGER.config("jersey-client bundle registers JAX-RS RuntimeDelegate");
                runtimeDelegate = (RuntimeDelegate) getClass().getClassLoader().
                        loadClass("com.sun.ws.rs.ext.RuntimeDelegateImpl").newInstance();
            } else {
                LOGGER.config("jersey-server bundle activator registers JAX-RS RuntimeDelegate instance");
                runtimeDelegate = (RuntimeDelegate) getClass().getClassLoader().
                        loadClass("com.sun.jersey.server.impl.provider.RuntimeDelegateImpl").newInstance();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to create RuntimeDelegate instance.", e);
        }

        RuntimeDelegate.setInstance(runtimeDelegate);
    }

    private Bundle getJerseyServerBundle(BundleContext bc) {
        for (Bundle b : bc.getBundles()) {
            final String symbolicName = b.getSymbolicName();
            if ((symbolicName != null)
                    && (symbolicName.endsWith("jersey-server") || symbolicName.endsWith("jersey-gf-server"))) {
                return b;
            }
        }
        return null;
    }

    private void registerExistingBundles() {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.STARTING
                    || bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STOPPING) {
                register(bundle);
            }
        }
    }

    private void setOSGiServiceFinderIteratorProvider() {
        ServiceFinder.setIteratorProvider(new OsgiServiceFinder());
    }

    private void register(final Bundle bundle) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "checking bundle {0}", bundle.getBundleId());
        }

        Map<String, Callable<List<Class<?>>>> map;
        lock.writeLock().lock();
        try {
            map = factories.get(bundle.getBundleId());
            if (map == null) {
                map = new ConcurrentHashMap<String, Callable<List<Class<?>>>>();
                factories.put(bundle.getBundleId(), map);
            }
        } finally {
            lock.writeLock().unlock();
        }

        Enumeration e = bundle.findEntries("META-INF/services/", "*", false);
        if (e != null) {
            while (e.hasMoreElements()) {
                final URL u = (URL) e.nextElement();
                final String url = u.toString();
                if (url.endsWith("/")) {
                    continue;
                }
                final String factoryId = url.substring(url.lastIndexOf("/") + 1);
                map.put(factoryId, new BundleSpiProvidersLoader(factoryId, u, bundle));
            }
        }
    }

    private List<Class<?>> locateAllProviders(String serviceName) {
        lock.readLock().lock();
        try {
            final List<Class<?>> result = new LinkedList<Class<?>>();
            for (Map<String, Callable<List<Class<?>>>> value : factories.values()) {
                if (value.containsKey(serviceName)) {
                    try {
                        result.addAll(value.get(serviceName).call());
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
}
