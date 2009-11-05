package com.sun.jersey.server.impl.jcdi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

public class JCDIComponentExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(
            JCDIComponentExtension.class.getName());

    private final List<ProcessInjectionTarget> l = new ArrayList<ProcessInjectionTarget>();

    public Collection<ProcessInjectionTarget> getProcessInjectionTargets() {
        return Collections.unmodifiableCollection(l);
    }

    public void clear() {
        l.clear();
    }

//    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
//        LOGGER.info("JERSEY: BEFORE BEAN DISCOVERY: " + this + " " + event.getClass());
//    }
//
//    void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
//        LOGGER.info("JERSEY: AFTER BEAN DISCOVERY: " + this + " " + manager.getClass());
//    }
//
//    void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
//        LOGGER.info("JERSEY: AFTER DEPLOYMENT VALIDATION: " + this);
//    }

    void processInjectionTarget(@Observes ProcessInjectionTarget<?> pit) {
        l.add(pit);
        
        LOGGER.info("JERSEY: PROCESS INJECTION TARGET: " + this + " " + pit + " " + pit.getAnnotatedType().getJavaClass());
    }
}