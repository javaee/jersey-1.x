package com.sun.jersey.server.impl.jcdi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessProducer;

public class JCDIComponentExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(
            JCDIComponentExtension.class.getName());

    private final List<ProcessInjectionTarget> l = new ArrayList<ProcessInjectionTarget>();

    public Collection<ProcessInjectionTarget> getProcessInjectionTargets() {
        return Collections.unmodifiableCollection(l);
    }
    
    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        LOGGER.info("JERSEY: BEFORE BEAN DISCOVERY: " + event.getClass());
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
        LOGGER.info("JERSEY: AFTER BEAN DISCOVERY: " + manager.getClass());
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
        LOGGER.info("JERSEY: AFTER DEPLOYMENT VALIDATION");
    }

    void processAnnotatedType(@Observes ProcessAnnotatedType<?> pat) {
        LOGGER.info("JERSEY: PROCESS ANNOTATED TYPE: " + pat.getAnnotatedType());
    }

    void processInjectionTarget(@Observes ProcessInjectionTarget<?> pit) {
        l.add(pit);
        
        LOGGER.info("JERSEY: PROCESS INJECTION TARGET: " + pit.getAnnotatedType());

        InjectionTarget<?> it = pit.getInjectionTarget();
        for (InjectionPoint ip : it.getInjectionPoints()) {
            LOGGER.info("  InjectionPoint: " + ip);
        }
    }

    void processProducer(@Observes ProcessProducer<?, ?> pp) {
        LOGGER.info("JERSEY: PROCESS PRODUCER: " + pp);
    }

    void processBean(@Observes ProcessManagedBean<?> pb) {
        LOGGER.info("JERSEY: PROCESS MANAGED BEAN: " + pb);
    }
}