package com.sun.jersey.server.impl.jcdi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

public class JCDIComponentExtension implements Extension {
    private final List<ProcessInjectionTarget> l = new ArrayList<ProcessInjectionTarget>();

    public Collection<ProcessInjectionTarget> getProcessInjectionTargets() {
        return Collections.unmodifiableCollection(l);
    }

    public void clear() {
        l.clear();
    }

    void processInjectionTarget(@Observes ProcessInjectionTarget<?> pit) {
        l.add(pit);
    }
}