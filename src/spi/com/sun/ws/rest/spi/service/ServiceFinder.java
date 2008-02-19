/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.spi.service;

import com.sun.ws.rest.spi.SpiMessages;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A simple service-provider lookup mechanism.  A <i>service</i> is a
 * well-known set of interfaces and (usually abstract) classes.  A <i>service
 * provider</i> is a specific implementation of a service.  The classes in a
 * provider typically implement the interfaces and subclass the classes defined
 * in the service itself.  Service providers may be installed in an
 * implementation of the Java platform in the form of extensions, that is, jar
 * files placed into any of the usual extension directories.  Providers may
 * also be made available by adding them to the applet or application class
 * path or by some other platform-specific means.
 * <p/>
 * <p> In this lookup mechanism a service is represented by an interface or an
 * abstract class.  (A concrete class may be used, but this is not
 * recommended.)  A provider of a given service contains one or more concrete
 * classes that extend this <i>service class</i> with data and code specific to
 * the provider.  This <i>provider class</i> will typically not be the entire
 * provider itself but rather a proxy that contains enough information to
 * decide whether the provider is able to satisfy a particular request together
 * with code that can create the actual provider on demand.  The details of
 * provider classes tend to be highly service-specific; no single class or
 * interface could possibly unify them, so no such class has been defined.  The
 * only requirement enforced here is that provider classes must have a
 * zero-argument constructor so that they may be instantiated during lookup.
 * <p/>
 * <p> A service provider identifies itself by placing a provider-configuration
 * file in the resource directory <tt>META-INF/services</tt>.  The file's name
 * should consist of the fully-qualified name of the abstract service class.
 * The file should contain a list of fully-qualified concrete provider-class
 * names, one per line.  Space and tab characters surrounding each name, as
 * well as blank lines, are ignored.  The comment character is <tt>'#'</tt>
 * (<tt>0x23</tt>); on each line all characters following the first comment
 * character are ignored.  The file must be encoded in UTF-8.
 * <p/>
 * <p> If a particular concrete provider class is named in more than one
 * configuration file, or is named in the same configuration file more than
 * once, then the duplicates will be ignored.  The configuration file naming a
 * particular provider need not be in the same jar file or other distribution
 * unit as the provider itself.  The provider must be accessible from the same
 * class loader that was initially queried to locate the configuration file;
 * note that this is not necessarily the class loader that found the file.
 * <p/>
 * <p> <b>Example:</b> Suppose we have a service class named
 * <tt>java.io.spi.CharCodec</tt>.  It has two abstract methods:
 * <p/>
 * <pre>
 *   public abstract CharEncoder getEncoder(String encodingName);
 *   public abstract CharDecoder getDecoder(String encodingName);
 * </pre>
 * <p/>
 * Each method returns an appropriate object or <tt>null</tt> if it cannot
 * translate the given encoding.  Typical <tt>CharCodec</tt> providers will
 * support more than one encoding.
 * <p/>
 * <p> If <tt>sun.io.StandardCodec</tt> is a provider of the <tt>CharCodec</tt>
 * service then its jar file would contain the file
 * <tt>META-INF/services/java.io.spi.CharCodec</tt>.  This file would contain
 * the single line:
 * <p/>
 * <pre>
 *   sun.io.StandardCodec    # Standard codecs for the platform
 * </pre>
 * <p/>
 * To locate an codec for a given encoding name, the internal I/O code would
 * do something like this:
 * <p/>
 * <pre>
 *   CharEncoder getEncoder(String encodingName) {
 *       for( CharCodec cc : ServiceFinder.find(CharCodec.class) ) {
 *           CharEncoder ce = cc.getEncoder(encodingName);
 *           if (ce != null)
 *               return ce;
 *       }
 *       return null;
 *   }
 * </pre>
 * <p/>
 * The provider-lookup mechanism always executes in the security context of the
 * caller.  Trusted system code should typically invoke the methods in this
 * class from within a privileged security context.
 *
 * @author Mark Reinhold
 * @version 1.11, 03/12/19
 * @since 1.3
 */
public final class ServiceFinder<T> implements Iterable<T> {
    private static final Logger LOGGER = Logger.getLogger(ServiceFinder.class.getName());
    
    private static final String PREFIX = "META-INF/services/";
    
    private static final ComponentProvider DEFAULT_COMPONENT_PROVIDER = new ComponentProvider() {
        public Object getInstance(Scope scope, Class c) 
                throws InstantiationException, IllegalAccessException {
            return c.newInstance();
        }

        public Object getInstance(Scope scope, Constructor contructor, Object[] parameters) 
                throws InstantiationException, IllegalArgumentException, 
                IllegalAccessException, InvocationTargetException {
            throw new UnsupportedOperationException("");
        }

        public void inject(Object instance) {
            throw new UnsupportedOperationException("");
        }
    };
            
    private final Class<T> serviceClass;
    private final ClassLoader classLoader;
    private final boolean ignoreOnClassNotFound;
    private final ComponentProvider componentProvider;
    
    
    /**
     * Locates and incrementally instantiates the available providers of a
     * given service using the given class loader.
     * <p/>
     * <p> This method transforms the name of the given service class into a
     * provider-configuration filename as described above and then uses the
     * <tt>getResources</tt> method of the given class loader to find all
     * available files with that name.  These files are then read and parsed to
     * produce a list of provider-class names.  The iterator that is returned
     * uses the given class loader to lookup and then instantiate each element
     * of the list.
     * <p/>
     * <p> Because it is possible for extensions to be installed into a running
     * Java virtual machine, this method may return different results each time
     * it is invoked. <p>
     * @param service The service's abstract service class
     * @param loader The class loader to be used to load provider-configuration files
     *                and instantiate provider classes, or <tt>null</tt> if the system
     *                class loader (or, failing that the bootstrap class loader) is to
     *                be used
     * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
     *                                   or names a provider class that cannot be found and instantiated
     * @see #find(Class)
     * @return the service finder
     */
    public static <T> ServiceFinder<T> find(Class<T> service, ClassLoader loader) 
            throws ServiceConfigurationError {
        return find(service, 
                loader, 
                false, 
                DEFAULT_COMPONENT_PROVIDER);
    }
    
    /**
     * Locates and incrementally instantiates the available providers of a
     * given service using the given class loader.
     * <p/>
     * <p> This method transforms the name of the given service class into a
     * provider-configuration filename as described above and then uses the
     * <tt>getResources</tt> method of the given class loader to find all
     * available files with that name.  These files are then read and parsed to
     * produce a list of provider-class names.  The iterator that is returned
     * uses the given class loader to lookup and then instantiate each element
     * of the list.
     * <p/>
     * <p> Because it is possible for extensions to be installed into a running
     * Java virtual machine, this method may return different results each time
     * it is invoked. <p>
     * @param service The service's abstract service class
     * @param loader The class loader to be used to load provider-configuration files
     *                and instantiate provider classes, or <tt>null</tt> if the system
     *                class loader (or, failing that the bootstrap class loader) is to
     *                be used
     * @param ignoreOnClassNotFound If a provider cannot be loaded by the class loader
     *                              then move on to the next available provider.
     * @param componentProvider the component provider responsible for instantiating
     *                          the provider implementation
     * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
     *                                   or names a provider class that cannot be found and instantiated
     * @see #find(Class)
     * @return the service finder
     */
    public static <T> ServiceFinder<T> find(Class<T> service, 
            ClassLoader loader, 
            boolean ignoreOnClassNotFound,
            ComponentProvider componentProvider) throws ServiceConfigurationError {
        return new ServiceFinder<T>(service,
                loader,
                ignoreOnClassNotFound,
                componentProvider);
    }
    
    /**
     * Locates and incrementally instantiates the available providers of a
     * given service using the context class loader.  This convenience method
     * is equivalent to
     * <p/>
     * <pre>
     *   ClassLoader cl = Thread.currentThread().getContextClassLoader();
     *   return Service.providers(service, cl, false);
     * </pre>
     * @param service The service's abstract service class
     * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
     *                                   or names a provider class that cannot be found and instantiated
     * @see #find(Class, ClassLoader)
     * @return the service finder
     */
    public static <T> ServiceFinder<T> find(Class<T> service) 
            throws ServiceConfigurationError {
        return find(service,
                Thread.currentThread().getContextClassLoader(), 
                false,
                DEFAULT_COMPONENT_PROVIDER);
    }
    
    /**
     * Locates and incrementally instantiates the available providers of a
     * given service using the context class loader.  This convenience method
     * is equivalent to
     * <p/>
     * <pre>
     *   ClassLoader cl = Thread.currentThread().getContextClassLoader();
     *   boolean ingore = ...
     *   return Service.providers(service, cl, ignore);
     * </pre>
     * @param service The service's abstract service class
     * @param ignoreOnClassNotFound If a provider cannot be loaded by the class loader
     *                              then move on to the next available provider.
     * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
     *                                   or names a provider class that cannot be found and instantiated
     * @see #find(Class, ClassLoader)
     * @return the service finder
     */
    public static <T> ServiceFinder<T> find(Class<T> service, 
            boolean ignoreOnClassNotFound) throws ServiceConfigurationError {
        return find(service,
                Thread.currentThread().getContextClassLoader(), 
                ignoreOnClassNotFound,
                DEFAULT_COMPONENT_PROVIDER);
    }
    
    /**
     * Locates and incrementally instantiates the available providers of a
     * given service using the context class loader.  This convenience method
     * is equivalent to
     * <p/>
     * <pre>
     *   ClassLoader cl = Thread.currentThread().getContextClassLoader();
     *   boolean ingore = ...
     *   return Service.providers(service, cl, ignore);
     * </pre>
     * @param service The service's abstract service class
     * @param ignoreOnClassNotFound If a provider cannot be loaded by the class loader
     *                              then move on to the next available provider.
     * @param componentProvider the component provider responsible for instantiating
     *                          the provider implementation
     * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
     *                                   or names a provider class that cannot be found and instantiated
     * @see #find(Class, ClassLoader)
     * @return the service finder
     */
    public static <T> ServiceFinder<T> find(Class<T> service, 
            boolean ignoreOnClassNotFound, ComponentProvider componentProvider) 
            throws ServiceConfigurationError {
        return find(service,
                Thread.currentThread().getContextClassLoader(), 
                ignoreOnClassNotFound,
                componentProvider);
    }
    
    private ServiceFinder(Class<T> service, ClassLoader loader, 
            boolean ignoreOnClassNotFound, ComponentProvider componentProvider) {
        this.serviceClass = service;
        this.classLoader = loader;
        this.ignoreOnClassNotFound = ignoreOnClassNotFound;
        this.componentProvider = componentProvider;
    }
    
    /**
     * Returns discovered objects incrementally.
     *
     * @return An <tt>Iterator</tt> that yields provider objects for the given
     *         service, in some arbitrary order.  The iterator will throw a
     *         <tt>ServiceConfigurationError</tt> if a provider-configuration
     *         file violates the specified format or if a provider class cannot
     *         be found and instantiated.
     */
    public Iterator<T> iterator() {
        return new LazyObjectIterator<T>(serviceClass,classLoader,
                ignoreOnClassNotFound, componentProvider);
    }
    
    /**
     * Returns discovered classes incrementally.
     *
     * @return An <tt>Iterator</tt> that yields provider classes for the given
     *         service, in some arbitrary order.  The iterator will throw a
     *         <tt>ServiceConfigurationError</tt> if a provider-configuration
     *         file violates the specified format or if a provider class cannot
     *         be found.
     */
    public Iterator<Class<T>> classIterator() {
        return new LazyClassIterator<T>(serviceClass,classLoader,
                ignoreOnClassNotFound, componentProvider);
    }
    
    /**
     * Returns discovered objects all at once.
     *
     * @return
     *      can be empty but never null.
     *
     * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
     *                                   or names a provider class that cannot be found and instantiated
     */
    @SuppressWarnings("unchecked")
    public T[] toArray() throws ServiceConfigurationError {
        List<T> result = new ArrayList<T>();
        for (T t : this) {
            result.add(t);
        }
        return result.toArray((T[])Array.newInstance(serviceClass,result.size()));
    }
    
    /**
     * Returns discovered classes all at once.
     *
     * @return
     *      can be empty but never null.
     *
     * @throws ServiceConfigurationError If a provider-configuration file violates the specified format
     *                                   or names a provider class that cannot be found
     */
    @SuppressWarnings("unchecked")
    public Class<T>[] toClassArray() throws ServiceConfigurationError {
        List<Class<T>> result = new ArrayList<Class<T>>();
        
        Iterator<Class<T>> i = classIterator();
        while (i.hasNext())
            result.add(i.next());
        return result.toArray((Class<T>[])Array.newInstance(Class.class,result.size()));
    }
    
    private static void fail(Class service, String msg, Throwable cause)
    throws ServiceConfigurationError {
        ServiceConfigurationError sce
                = new ServiceConfigurationError(service.getName() + ": " + msg);
        sce.initCause(cause);
        throw sce;
    }
    
    private static void fail(Class service, String msg)
    throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }
    
    private static void fail(Class service, URL u, int line, String msg)
    throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }
    
    /**
     * Parse a single line from the given configuration file, adding the name
     * on the line to both the names list and the returned set iff the name is
     * not already a member of the returned set.
     */
    private static int parseLine(Class service, URL u, BufferedReader r, int lc,
            List<String> names, Set<String> returned)
            throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                fail(service, u, lc, SpiMessages.ILLEGAL_CONFIG_SYNTAX());
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                fail(service, u, lc, SpiMessages.ILLEGAL_PROVIDER_CLASS_NAME(ln));
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(service, u, lc, SpiMessages.ILLEGAL_PROVIDER_CLASS_NAME(ln));
            }
            if (!returned.contains(ln)) {
                names.add(ln);
                returned.add(ln);
            }
        }
        return lc + 1;
    }
    
    /**
     * Parse the content of the given URL as a provider-configuration file.
     *
     * @param service  The service class for which providers are being sought;
     *                 used to construct error detail strings
     * @param u        The URL naming the configuration file to be parsed
     * @param returned A Set containing the names of provider classes that have already
     *                 been returned.  This set will be updated to contain the names
     *                 that will be yielded from the returned <tt>Iterator</tt>.
     * @return A (possibly empty) <tt>Iterator</tt> that will yield the
     *         provider-class names in the given configuration file that are
     *         not yet members of the returned set
     * @throws ServiceConfigurationError If an I/O error occurs while reading from the given URL, or
     *                                   if a configuration-file format error is detected
     */
    @SuppressWarnings({"StatementWithEmptyBody"})
    private static Iterator<String> parse(Class service, URL u, Set<String> returned)
    throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = new ArrayList<String>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, u, r, lc, names, returned)) >= 0) ;
        } catch (IOException x) {
            fail(service, ": " + x);
        } finally {
            try {
                if (r != null) r.close();
                if (in != null) in.close();
            } catch (IOException y) {
                fail(service, ": " + y);
            }
        }
        return names.iterator();
    }
    
    private static class AbstractLazyIterator<T> {
        final Class<T> service;
        final ClassLoader loader;
        final boolean ignoreOnClassNotFound;
        final ComponentProvider componentProvider;
        
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        Set<String> returned = new TreeSet<String>();
        String nextName = null;
        
        private AbstractLazyIterator(Class<T> service, ClassLoader loader, 
                boolean ignoreOnClassNotFound, ComponentProvider componentProvider) {
            this.service = service;
            this.loader = loader;
            this.ignoreOnClassNotFound = ignoreOnClassNotFound;
            this.componentProvider = componentProvider;
        }
        
        public boolean hasNext() throws ServiceConfigurationError {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);
                } catch (IOException x) {
                    fail(service, ": " + x);
                }
            }
            
            while (nextName == null) {
                while ((pending == null) || !pending.hasNext()) {
                    if (!configs.hasMoreElements()) {
                        return false;
                    }
                    pending = parse(service, configs.nextElement(), returned);
                }
                nextName = pending.next();
                if (ignoreOnClassNotFound) {
                    try {
                        Class.forName(nextName, true, loader);
                    } catch (ClassNotFoundException ex) {
                        // Provider implementation not found
                        if(LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING, 
                                    SpiMessages.PROVIDER_NOT_FOUND(nextName, service));
                        }
                        nextName = null;
                    } catch (NoClassDefFoundError ex) {
                        // Dependent class of provider not found
                        if(LOGGER.isLoggable(Level.WARNING)) {
                            // This assumes that ex.getLocalizedMessage() returns
                            // the name of a dependent class that is not found
                            LOGGER.log(Level.WARNING , 
                                    SpiMessages.DEPENDENT_CLASS_OF_PROVIDER_NOT_FOUND(
                                    ex.getLocalizedMessage(), nextName, service));
                        }
                        nextName = null; 
                    }
                }
            }
            return true;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }        
    }
    
    private static final class LazyClassIterator<T> extends AbstractLazyIterator<T> 
            implements Iterator<Class<T>> {

        private LazyClassIterator(Class<T> service, ClassLoader loader, 
                boolean ignoreOnClassNotFound, ComponentProvider componentProvider) {
            super(service, loader, ignoreOnClassNotFound, componentProvider);
        }
        
        @SuppressWarnings("unchecked")
        public Class<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String cn = nextName;
            nextName = null;
            try {
                return (Class<T>)Class.forName(cn, true, loader);
            } catch (ClassNotFoundException ex) {
                fail(service, 
                        SpiMessages.PROVIDER_NOT_FOUND(cn, service));
            } catch (NoClassDefFoundError ex) {
                fail(service,
                        SpiMessages.DEPENDENT_CLASS_OF_PROVIDER_NOT_FOUND(
                        ex.getLocalizedMessage(), cn, service));
            } catch (Exception x) {
                fail(service,
                        SpiMessages.PROVIDER_CLASS_COULD_NOT_BE_LOADED(cn, service, x.getLocalizedMessage()),
                        x);
            }
            
            return null;    /* This cannot happen */
        }
    }
    
    private static final class LazyObjectIterator<T> extends AbstractLazyIterator<T> 
            implements Iterator<T> {

        private LazyObjectIterator(Class<T> service, ClassLoader loader, 
                boolean ignoreOnClassNotFound, ComponentProvider componentProvider) {
            super(service, loader, ignoreOnClassNotFound, componentProvider);
        }
        
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String cn = nextName;
            nextName = null;
            try {
                return service.cast(componentProvider.getInstance(null, 
                        Class.forName(cn, true, loader)));
            } catch (ClassNotFoundException x) {
                fail(service, 
                        SpiMessages.PROVIDER_NOT_FOUND(cn, service));
            } catch (Exception x) {
                fail(service,
                        SpiMessages.PROVIDER_COULD_NOT_BE_CREATED(cn, service, x.getLocalizedMessage()),
                        x);
            }
            return null;    /* This cannot happen */
        }
    }
}
