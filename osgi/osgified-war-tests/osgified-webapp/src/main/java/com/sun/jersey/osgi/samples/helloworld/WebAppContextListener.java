/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.osgi.samples.helloworld;

import java.util.HashMap;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 *
 * @author japod
 */
public class WebAppContextListener implements BundleActivator, ServletContextListener {

    static EventAdmin ea;

    BundleContext bc;
    ServiceReference eaRef;

    synchronized static EventAdmin getEa() {
        return ea;
    }

    synchronized static void setEa(EventAdmin ea) {
        WebAppContextListener.ea = ea;
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        if (getEa() != null) {
            getEa().sendEvent(new Event("jersey/test/DEPLOYED",new HashMap<String, String>(){{put("context-path", sce.getServletContext().getContextPath());}}));
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        if (getEa() != null) {
            getEa().sendEvent(new Event("jersey/test/UNDEPLOYED",new HashMap<String, String>(){{put("context-path", sce.getServletContext().getContextPath());}}));
        }
    }

    @Override
    public void start(BundleContext context) throws Exception {
        bc = context;
        eaRef = bc.getServiceReference(EventAdmin.class.getName());
        if (eaRef != null) {
            setEa((EventAdmin)bc.getService(eaRef));
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (eaRef != null) {
            bc.ungetService(eaRef);
        }
    }
}
