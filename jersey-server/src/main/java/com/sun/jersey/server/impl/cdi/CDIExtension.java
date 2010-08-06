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
 
package com.sun.jersey.server.impl.cdi;
 
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.container.ExceptionMapperContext;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.inject.Injectable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.ManagedBean;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
/**
 *
 * @author robc
 */
public class CDIExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(CDIExtension.class.getName());

    private static class ContextAnnotationLiteral extends AnnotationLiteral<Context> implements Context {};
    private final Context contextAnnotationLiteral = new ContextAnnotationLiteral();

    private static class InjectAnnotationLiteral extends AnnotationLiteral<Inject> implements Inject {};
    private final Inject injectAnnotationLiteral = new InjectAnnotationLiteral();

    private static class SyntheticQualifierAnnotationImpl extends AnnotationLiteral<SyntheticQualifier> implements SyntheticQualifier {
        private int value;

        public SyntheticQualifierAnnotationImpl(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    private WebApplication webApplication;
    private ResourceConfig resourceConfig;
    private Set<Class<? extends Annotation>> knownParameterQualifiers;
    private Set<Class<?>> staticallyDefinedContextBeans;
    private Map<Class<? extends Annotation>, Parameter.Source> paramQualifiersMap;

    private Map<Class<? extends Annotation>, Set<DiscoveredParameter>> discoveredParameterMap;
    private Map<DiscoveredParameter, SyntheticQualifier> syntheticQualifierMap;
    private int nextSyntheticQualifierValue = 0;
    
    private List<InitializedLater> toBeInitializedLater;

    public CDIExtension() {}

    private void initialize() {
        // initialize in a separate method because the META-INF/service mechanism
        // seems to cause a spurious instance to be created and we don't want to
        // waste cycles initializing it

        // annotations to be turned into qualifiers
        Set<Class<? extends Annotation>> set = new HashSet<Class<? extends Annotation>>();
        set.add(CookieParam.class);
        set.add(FormParam.class);
        set.add(HeaderParam.class);
        set.add(MatrixParam.class);
        set.add(PathParam.class);
        set.add(QueryParam.class);
        set.add(Context.class);
        knownParameterQualifiers = Collections.unmodifiableSet(set);

        // used to map a qualifier to a Parameter.Source
        Map<Class<? extends Annotation>, Parameter.Source> map = new HashMap<Class<? extends Annotation>, Parameter.Source>();
        map.put(CookieParam.class,Parameter.Source.COOKIE);
        map.put(FormParam.class,Parameter.Source.FORM);
        map.put(HeaderParam.class,Parameter.Source.HEADER);
        map.put(MatrixParam.class,Parameter.Source.MATRIX);
        map.put(PathParam.class,Parameter.Source.PATH);
        map.put(QueryParam.class,Parameter.Source.QUERY);
        map.put(Context.class,Parameter.Source.CONTEXT);
        paramQualifiersMap = Collections.unmodifiableMap(map);
        
        // pre-defined contextual types
        Set<Class<?>> set3 = new HashSet<Class<?>>();
        // standard types
        set3.add(Application.class);
        set3.add(HttpHeaders.class);
        set3.add(Providers.class);
        set3.add(Request.class);
        set3.add(SecurityContext.class);
        set3.add(UriInfo.class);
        // Jersey extensions
        set3.add(ExceptionMapperContext.class);
        set3.add(ExtendedUriInfo.class);
        set3.add(FeaturesAndProperties.class);
        set3.add(HttpContext.class);
        set3.add(HttpRequestContext.class);
        set3.add(HttpResponseContext.class);
        set3.add(MessageBodyWorkers.class);
        set3.add(WebApplication.class);
        staticallyDefinedContextBeans = Collections.unmodifiableSet(set3);

        // tracks all discovered parameters
        Map<Class<? extends Annotation>, Set<DiscoveredParameter>> map2 = new HashMap<Class<? extends Annotation>, Set<DiscoveredParameter>>();
        for (Class<? extends Annotation> qualifier : knownParameterQualifiers) {
            map2.put(qualifier, new HashSet<DiscoveredParameter>());
        }
        discoveredParameterMap = Collections.unmodifiableMap(map2);

        // tracks the synthetic qualifiers we have to create to handle a specific
        // combination of JAX-RS injection annotation + default value + encoded
        syntheticQualifierMap = new HashMap<DiscoveredParameter, SyntheticQualifier>();

        // things to do in a second time, i.e. once Jersey has been initialized,
        // as opposed to when CDI delivers the SPI events to its extensions
        toBeInitializedLater = new ArrayList<InitializedLater>();
    }

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        LOGGER.fine("Handling BeforeBeanDiscovery event");
        initialize();

        // turn JAX-RS injection annotations into CDI qualifiers
        for (Class<? extends Annotation> qualifier : knownParameterQualifiers) {
            event.addQualifier(qualifier);
        }
    }

   /*
    *  Holds information on one site (constructor/method argument or field) to be patched.
    */
    private static class PatchInformation {
        private DiscoveredParameter parameter;
        private SyntheticQualifier syntheticQualifier;
        private boolean mustAddInject;

        public PatchInformation(DiscoveredParameter parameter, SyntheticQualifier syntheticQualifier, boolean mustAddInject) {
            this.parameter = parameter;
            this.syntheticQualifier = syntheticQualifier;
            this.mustAddInject = mustAddInject;
        }

        public DiscoveredParameter getParameter() {
            return parameter;
        }

        public SyntheticQualifier getSyntheticQualifier() {
            return syntheticQualifier;
        }

        public boolean mustAddInject() {
            return mustAddInject;
        }
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {
        LOGGER.fine("Handling ProcessAnnotatedType event for " + event.getAnnotatedType().getJavaClass().getName());

        AnnotatedType<T> type = event.getAnnotatedType();

        // only scan managed beans
        if (!type.isAnnotationPresent(ManagedBean.class)) {
            return;
        }

        // only scan root resource classes for now
        if (!type.isAnnotationPresent(Path.class)) {
            return;
        }

        // first pass to determine if we need to patch any sites
        // we also record any qualifiers with parameters we encounter
        // so we can create beans for them later

        // TODO - maybe we should detect cases in which the constructor selection
        // rules in CDI and JAX-RS are in conflict -- CDI should win, but
        // the result may surprise the user

        boolean classHasEncodedAnnotation = type.isAnnotationPresent(Encoded.class);

        Set<AnnotatedConstructor<T>> mustPatchConstructors = new HashSet<AnnotatedConstructor<T>>();
        Map<AnnotatedParameter<T>, PatchInformation> parameterToPatchInfoMap = new HashMap<AnnotatedParameter<T>, PatchInformation>();

        for (AnnotatedConstructor<T> constructor : type.getConstructors()) {
            if (processAnnotatedCallable(constructor, classHasEncodedAnnotation, parameterToPatchInfoMap)) {
                mustPatchConstructors.add(constructor);
            }
        }

        Set<AnnotatedField<T>> mustPatchFields = new HashSet<AnnotatedField<T>>();
        Map<AnnotatedField<T>, PatchInformation> fieldToPatchInfoMap = new HashMap<AnnotatedField<T>, PatchInformation>();

        outer:
        for (AnnotatedField<? super T> field : type.getFields()) {
            if (field.getDeclaringType() == type) {
                if (processAnnotatedField((AnnotatedField<T>) field, classHasEncodedAnnotation, fieldToPatchInfoMap)) {
                    mustPatchFields.add((AnnotatedField<T>)field);
                }
            }
        }

        Set<AnnotatedMethod<T>> mustPatchMethods = new HashSet<AnnotatedMethod<T>>();

        for (AnnotatedMethod<? super T> method : type.getMethods()) {
            if (method.getDeclaringType() == type) {
                if (processAnnotatedCallable((AnnotatedMethod<T>)method, classHasEncodedAnnotation, parameterToPatchInfoMap)) {
                    mustPatchMethods.add((AnnotatedMethod<T>)method);
                }
            }
        }

        boolean typeNeedsPatching = !(mustPatchConstructors.isEmpty() && mustPatchFields.isEmpty() && mustPatchMethods.isEmpty());

        // second pass
        if (typeNeedsPatching) {
            AnnotatedTypeImpl<T> newType = new AnnotatedTypeImpl(type);

            Set<AnnotatedConstructor<T>> newConstructors = new HashSet<AnnotatedConstructor<T>>();
            for (AnnotatedConstructor<T> constructor : type.getConstructors()) {
                AnnotatedConstructorImpl<T> newConstructor = new AnnotatedConstructorImpl(constructor, newType);
                if (mustPatchConstructors.contains(constructor)) {
                    patchAnnotatedCallable(constructor, newConstructor, parameterToPatchInfoMap);
                }
                else {
                    copyParametersOfAnnotatedCallable(constructor, newConstructor);
                }
                newConstructors.add(newConstructor);
            }

            Set<AnnotatedField<? super T>> newFields = new HashSet<AnnotatedField<? super T>>();
            for (AnnotatedField<? super T> field : type.getFields()) {
                if (field.getDeclaringType() == type) {
                    if (mustPatchFields.contains((AnnotatedField<T>)field)) {
                        PatchInformation patchInfo = fieldToPatchInfoMap.get((AnnotatedField<T>)field);
                        Set<Annotation> annotations = new HashSet<Annotation>();
                        if (patchInfo.mustAddInject()) {
                           annotations.add(injectAnnotationLiteral);
                        }
                        if (patchInfo.getSyntheticQualifier() != null) {
                           annotations.add(patchInfo.getSyntheticQualifier());
                           Annotation skippedQualifier = patchInfo.getParameter().getAnnotation();
                           for (Annotation annotation : field.getAnnotations()) {
                               if (annotation != skippedQualifier) {
                                   annotations.add(annotation);
                               }
                           }
                        }
                        else {
                            annotations.addAll(field.getAnnotations());
                        }
                        newFields.add(new AnnotatedFieldImpl<T>(field, annotations, newType));
                    }
                    else {
                        // copy and reparent
                        newFields.add(new AnnotatedFieldImpl<T>(field, newType));
                    }
                }
                else {
                    // simple copy
                    newFields.add(field);
                }
            }

            Set<AnnotatedMethod<? super T>> newMethods = new HashSet<AnnotatedMethod<? super T>>();
            for (AnnotatedMethod<? super T> method : type.getMethods()) {
                if (method.getDeclaringType() == type) {
                    AnnotatedMethodImpl<T> newMethod = new AnnotatedMethodImpl<T>(method, newType);
                    if (mustPatchMethods.contains((AnnotatedMethod<T>)method)) {
                        patchAnnotatedCallable((AnnotatedMethod<T>)method, newMethod, parameterToPatchInfoMap);
                    }
                    else {
                        copyParametersOfAnnotatedCallable((AnnotatedMethod<T>)method, newMethod);
                    }
                    newMethods.add(newMethod);
                }
                else {
                    // simple copy
                    newMethods.add(method);
                }

            }

            newType.setConstructors(newConstructors);
            newType.setFields(newFields);
            newType.setMethods(newMethods);
            event.setAnnotatedType(newType);
            
            LOGGER.fine("  replaced annotated type for " + type.getJavaClass());
        }
    }

    private <T> boolean processAnnotatedCallable(AnnotatedCallable<T> callable,
                                                 boolean classHasEncodedAnnotation,
                                                 Map<AnnotatedParameter<T>, PatchInformation> parameterToPatchInfoMap) {
        boolean mustPatch = false;

        if (callable.getAnnotation(Inject.class) != null) {
            boolean methodHasEncodedAnnotation = callable.isAnnotationPresent(Encoded.class);
            for (AnnotatedParameter<T> parameter : callable.getParameters()) {
                for (Annotation annotation : parameter.getAnnotations()) {
                    Set<DiscoveredParameter> discovered = discoveredParameterMap.get(annotation.annotationType());
                    if (discovered != null) {
                        if (knownParameterQualifiers.contains(annotation.annotationType())) {
                            if (methodHasEncodedAnnotation ||
                                classHasEncodedAnnotation ||
                                parameter.isAnnotationPresent(DefaultValue.class)) {
                                mustPatch = true;
                            }

                            boolean encoded = parameter.isAnnotationPresent(Encoded.class) || methodHasEncodedAnnotation || classHasEncodedAnnotation;
                            DefaultValue defaultValue = parameter.getAnnotation(DefaultValue.class);
                            if (defaultValue != null) {
                                mustPatch = true;
                            }
                            DiscoveredParameter jerseyParameter = new DiscoveredParameter(annotation, parameter.getBaseType(), defaultValue, encoded);
                            discovered.add(jerseyParameter);
                            LOGGER.fine("  recorded " + jerseyParameter);
                            parameterToPatchInfoMap.put(parameter, new PatchInformation(jerseyParameter, getSyntheticQualifierFor(jerseyParameter), false));
                        }
                    }
                }
            }
        }
        
        return mustPatch;
    }

    private <T> boolean processAnnotatedField(AnnotatedField<T> field,
                                              boolean classHasEncodedAnnotation,
                                              Map<AnnotatedField<T>, PatchInformation> fieldToPatchInfoMap) {
        boolean mustPatch = false;
        for (Annotation annotation : field.getAnnotations()) {
            if (knownParameterQualifiers.contains(annotation.annotationType())) {
                boolean mustAddInjectAnnotation = !field.isAnnotationPresent(Inject.class);

                if (field.isAnnotationPresent(Encoded.class) ||
                    classHasEncodedAnnotation ||
                    mustAddInjectAnnotation ||
                    field.isAnnotationPresent(DefaultValue.class)) {
                    mustPatch = true;
                }

                Set<DiscoveredParameter> discovered = discoveredParameterMap.get(annotation.annotationType());
                if (discovered != null) {
                    boolean encoded = field.isAnnotationPresent(Encoded.class) || classHasEncodedAnnotation;
                    DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
                    DiscoveredParameter parameter = new DiscoveredParameter(annotation, field.getBaseType(), defaultValue, encoded);
                    discovered.add(parameter);
                    LOGGER.fine("  recorded " + parameter);
                    fieldToPatchInfoMap.put(field, new PatchInformation(parameter, getSyntheticQualifierFor(parameter), mustAddInjectAnnotation));
                }
            }
        }
        
        return mustPatch;
    }

    private <T> void patchAnnotatedCallable(AnnotatedCallable<T> callable,
                                            AnnotatedCallableImpl<T> newCallable,
                                            Map<AnnotatedParameter<T>, PatchInformation> parameterToPatchInfoMap) {
        List<AnnotatedParameter<T>> newParams = new ArrayList<AnnotatedParameter<T>>();
        for (AnnotatedParameter<T> parameter : callable.getParameters()) {
            PatchInformation patchInfo = parameterToPatchInfoMap.get(parameter);
            if (patchInfo != null) {
                Set<Annotation> annotations = new HashSet<Annotation>();
                // in reality, this cannot happen
                if (patchInfo.mustAddInject()) {
                   annotations.add(injectAnnotationLiteral);
                }
                
                if (patchInfo.getSyntheticQualifier() != null) {
                   annotations.add(patchInfo.getSyntheticQualifier());
                   Annotation skippedQualifier = patchInfo.getParameter().getAnnotation();
                   for (Annotation annotation : parameter.getAnnotations()) {
                       if (annotation != skippedQualifier) {
                           annotations.add(annotation);
                       }
                   }
                }
                else {
                    annotations.addAll(parameter.getAnnotations());
                }
                newParams.add(new AnnotatedParameterImpl<T>(parameter, annotations, callable));
            }
            else {
                newParams.add(new AnnotatedParameterImpl<T>(parameter, newCallable));
            }
        }
        newCallable.setParameters(newParams);
    }

    private <T> void copyParametersOfAnnotatedCallable(AnnotatedCallable<T> callable, AnnotatedCallableImpl<T> newCallable) {
        // copy and reparent all the parameters
        List<AnnotatedParameter<T>> newParams = new ArrayList<AnnotatedParameter<T>>();
        for (AnnotatedParameter<T> parameter : callable.getParameters()) {
            newParams.add(new AnnotatedParameterImpl<T>(parameter, newCallable));
        }
        newCallable.setParameters(newParams);
    }

    private SyntheticQualifier getSyntheticQualifierFor(DiscoveredParameter parameter) {
        SyntheticQualifier result = syntheticQualifierMap.get(parameter);
        if (result == null) {
            // only create a synthetic qualifier if we're dealing with @DefaultValue
            // or @Encoded; this way the application can still use vanilla param
            // annotations as qualifiers
            if (parameter.isEncoded() || parameter.getDefaultValue() != null) {
                result = new SyntheticQualifierAnnotationImpl(nextSyntheticQualifierValue++);
                syntheticQualifierMap.put(parameter, result);
                LOGGER.fine("  created synthetic qualifier " + result);
            }
        }
        return result;
    }
    
    // taken from ReflectionHelper
    private static Class getClassOfType(Type type) {
        if (type instanceof Class) {
            return (Class)type;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType)type;
            Type t = arrayType.getGenericComponentType();
            if (t instanceof Class) {
                Class c = (Class)t;
                try {
                    // TODO is there a better way to get the Class object
                    // representing an array
                    Object o = Array.newInstance(c, 0);
                    return o.getClass();
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType subType = (ParameterizedType)type;
            Type t = subType.getRawType();
            if (t instanceof Class) {
                return (Class)t;
            }
        }
        return null;
    }

    <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> event) {
        LOGGER.fine("Handling ProcessInjectionTarget event for " + event.getAnnotatedType().getJavaClass().getName());
    }
    
    /*
    void processBean(@Observes ProcessBean<?> event) {
        LOGGER.fine("Handling ProcessBean event for " + event.getBean().getBeanClass().getName());
    }
    */

    void processManagedBean(@Observes ProcessManagedBean<?> event) {
        LOGGER.fine("Handling ProcessManagedBean event for " + event.getBean().getBeanClass().getName());

        // TODO - here we should check that all the rules have been followed
        // and call addDefinitionError for each problem we encountered
        
        Bean<?> bean = event.getBean();
        for (InjectionPoint injectionPoint : bean.getInjectionPoints()) {
            StringBuilder sb = new StringBuilder();
            sb.append("  found injection point ");
            sb.append(injectionPoint.getType());
            for (Annotation annotation : injectionPoint.getQualifiers()) {
                sb.append(" ");
                sb.append(annotation);
            }
            LOGGER.fine(sb.toString());
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        LOGGER.fine("Handling AfterBeanDiscovery event");

        addPredefinedContextBeans(event);
        
        // finally define beans for all qualifiers we discovered
        
        BeanGenerator beanGenerator = new BeanGenerator("com/sun/jersey/server/impl/cdi/generated/Bean");

        for (Map.Entry<Class<? extends Annotation>, Set<DiscoveredParameter>> entry  : discoveredParameterMap.entrySet()) {
            Class<? extends Annotation> qualifier = entry.getKey();
            for (DiscoveredParameter parameter : entry.getValue()) {
                Annotation annotation = parameter.getAnnotation();
                Class<?> klass = getClassOfType(parameter.getType());

                if (annotation.annotationType() == Context.class &&
                        staticallyDefinedContextBeans.contains(klass) &&
                        !parameter.isEncoded() &&
                        parameter.getDefaultValue() == null) {
                    continue;
                }

                SyntheticQualifier syntheticQualifier = syntheticQualifierMap.get(parameter);
                Annotation theQualifier = syntheticQualifier != null ? syntheticQualifier : annotation;

                Set<Annotation> annotations = new HashSet<Annotation>();
                annotations.add(theQualifier);

                // TODO - here we pass a single annotation as the second argument,
                // i.e. the qualifier itself, but to be true to Jersey semantics we
                // should pass in all the annotations that were on the original program
                // element.
                // The problem here is that (1) we don't have the original program
                // element any more and (2) a single DiscoveredParameter may have
                // been encountered in multiple places with different annotations
                
                Parameter jerseyParameter = new Parameter(
                                                    new Annotation[]{ annotation },
                                                    annotation,
                                                    paramQualifiersMap.get(annotation.annotationType()),
                                                    parameter.getValue(),
                                                    parameter.getType(),
                                                    klass,
                                                    parameter.isEncoded(),
                                                    (parameter.getDefaultValue() == null ? null : parameter.getDefaultValue().value()));
                Class<?> beanClass = beanGenerator.createBeanClass();
                ParameterBean bean = new ParameterBean(beanClass, parameter.getType(), annotations, parameter, jerseyParameter);
                toBeInitializedLater.add(bean);
                event.addBean(bean);
                LOGGER.fine("Added bean for parameter " + parameter + " and qualifier " + theQualifier);
            }
        }

    }

    /*
     * Adds a CDI bean for each @Context type we support out of the box
     */
    private void addPredefinedContextBeans(AfterBeanDiscovery event) {
        // all standard types first

        // @Context Application
        event.addBean(new PredefinedBean<Application>(Application.class, contextAnnotationLiteral));

        // @Context HttpHeaders
        event.addBean(new ProviderBasedBean<HttpHeaders>(HttpHeaders.class, new Provider<HttpHeaders>() {
            public HttpHeaders get() {
                return webApplication.getThreadLocalHttpContext().getRequest();
            }
        }, contextAnnotationLiteral));

        // @Context Providers
        event.addBean(new ProviderBasedBean<Providers>(Providers.class, new Provider<Providers>() {
            public Providers get() {
                return webApplication.getProviders();
            }
        }, contextAnnotationLiteral));

        // @Context Request
        event.addBean(new ProviderBasedBean<Request>(Request.class, new Provider<Request>() {
            public Request get() {
                return webApplication.getThreadLocalHttpContext().getRequest();
            }
        }, contextAnnotationLiteral));

        // @Context SecurityContext
        event.addBean(new ProviderBasedBean<SecurityContext>(SecurityContext.class, new Provider<SecurityContext>() {
            public SecurityContext get() {
                return webApplication.getThreadLocalHttpContext().getRequest();
            }
        }, contextAnnotationLiteral));        

        // @Context UriInfo
        event.addBean(new ProviderBasedBean<UriInfo>(UriInfo.class, new Provider<UriInfo>() {
            public UriInfo get() {
                return webApplication.getThreadLocalHttpContext().getUriInfo();
            }
        }, contextAnnotationLiteral));
        
        // now the Jersey extensions

        // @Context ExceptionMapperContext
        event.addBean(new ProviderBasedBean<ExceptionMapperContext>(ExceptionMapperContext.class, new Provider<ExceptionMapperContext>() {
            public ExceptionMapperContext get() {
                return webApplication.getExceptionMapperContext();
            }
        }, contextAnnotationLiteral));

        // @Context ExtendedUriInfo
        event.addBean(new ProviderBasedBean<ExtendedUriInfo>(ExtendedUriInfo.class, new Provider<ExtendedUriInfo>() {
            public ExtendedUriInfo get() {
                return webApplication.getThreadLocalHttpContext().getUriInfo();
            }
        }, contextAnnotationLiteral));
        
        // @Context FeaturesAndProperties
        event.addBean(new ProviderBasedBean<FeaturesAndProperties>(FeaturesAndProperties.class, new Provider<FeaturesAndProperties>() {
            public FeaturesAndProperties get() {
                return webApplication.getFeaturesAndProperties();
            }
        }, contextAnnotationLiteral));

        // @Context HttpContext
        event.addBean(new ProviderBasedBean<HttpContext>(HttpContext.class, new Provider<HttpContext>() {
            public HttpContext get() {
                return webApplication.getThreadLocalHttpContext();
            }
        }, contextAnnotationLiteral));

        // @Context HttpRequestContext
        event.addBean(new ProviderBasedBean<HttpRequestContext>(HttpRequestContext.class, new Provider<HttpRequestContext>() {
            public HttpRequestContext get() {
                return webApplication.getThreadLocalHttpContext().getRequest();
            }
        }, contextAnnotationLiteral));

        // @Context HttpResponseContext
        event.addBean(new ProviderBasedBean<HttpResponseContext>(HttpResponseContext.class, new Provider<HttpResponseContext>() {
            public HttpResponseContext get() {
                return webApplication.getThreadLocalHttpContext().getResponse();
            }
        }, contextAnnotationLiteral));

        // @Context MessageBodyWorkers
        event.addBean(new ProviderBasedBean<MessageBodyWorkers>(MessageBodyWorkers.class, new Provider<MessageBodyWorkers>() {
            public MessageBodyWorkers get() {
                return webApplication.getMessageBodyWorkers();
            }
        }, contextAnnotationLiteral));

        // @Context WebApplication
        event.addBean(new ProviderBasedBean<WebApplication>(WebApplication.class, new Provider<WebApplication>() {
            public WebApplication get() {
                return webApplication;
            }
        }, contextAnnotationLiteral));

    }
    
    void setWebApplication(WebApplication wa) {
        webApplication = wa;
    }
    
    WebApplication getWebApplication() {
        return webApplication;
    }
    
    void setResourceConfig(ResourceConfig rc) {
        resourceConfig = rc;
    }
    
    ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    /*
     * Called after the WebApplication and ResourceConfig have been set,
     * i.e. when Jersey is in a somewhat initialized state.
     * 
     * By contrast, all the CDI driven code earlier in this source file
     * runs before Jersey gets a chance to initialize itself.
     */
    void lateInitialize() {
        for (InitializedLater object : toBeInitializedLater) {
            object.later();
        }
    }
    
    /*
     * Constructs an object by delegating to the ServerInjectableProviderFactory of the WebApplication
     */
    class PredefinedBean<T> extends AbstractBean<T> {

        private Annotation qualifier;

        public PredefinedBean(Class<T> klass, Annotation qualifier) {
            super(klass, qualifier);
            this.qualifier = qualifier;
        }

        @Override
        public T create(CreationalContext<T> creationalContext) {
            Injectable<T> injectable = webApplication.getServerInjectableProviderFactory().getInjectable(qualifier.annotationType(), null, qualifier, getBeanClass(), ComponentScope.Singleton);
            return injectable.getValue();
        }
    }

    /*
     * Constructs an object by delegating to the Injectable for a Jersey parameter
     */
    class ParameterBean<T> extends AbstractBean<T> implements InitializedLater {
        private DiscoveredParameter discoveredParameter;
        private Parameter parameter;
        private Injectable<T> injectable;

        public ParameterBean(Class<?> klass, Type type, Set<Annotation> qualifiers, DiscoveredParameter discoveredParameter, Parameter parameter) {
            super(klass, type, qualifiers);
            this.discoveredParameter = discoveredParameter;
            this.parameter = parameter;
        }

        public void later() {
            boolean registered = webApplication.getServerInjectableProviderFactory().isParameterTypeRegistered(parameter);
            if (!registered) {
                throw new ContainerException("parameter type not registered " + discoveredParameter);
            }
            // TODO - here it just doesn't seem possible to remove the cast
            injectable = (Injectable<T>) webApplication.getServerInjectableProviderFactory().getInjectable(parameter, ComponentScope.PerRequest);
            if (injectable == null) {
                throw new ContainerException("no injectable for parameter " + discoveredParameter);
            }
        }

        @Override public T create(CreationalContext<T> creationalContext) {
            try {
                return injectable.getValue();
            }
            catch (IllegalStateException e) {
                if (injectable instanceof AbstractHttpContextInjectable) {
                    return (T)((AbstractHttpContextInjectable)injectable).getValue(webApplication.getThreadLocalHttpContext());
                }
                else {
                    throw e;
                }
            }
        }
    }
}
