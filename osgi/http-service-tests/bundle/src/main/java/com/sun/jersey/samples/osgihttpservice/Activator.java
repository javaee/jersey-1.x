package com.sun.jersey.samples.osgihttpservice;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    private BundleContext bc;
    private ServiceTracker tracker;
    private HttpService httpService = null;
    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public synchronized void start(BundleContext bundleContext) throws Exception {
        this.bc = bundleContext;

        logger.info("STARTING HTTP SERVICE BUNDLE");

        this.tracker = new ServiceTracker(this.bc, HttpService.class.getName(), null) {

          @Override
          public Object addingService(ServiceReference serviceRef) {
             httpService = (HttpService)super.addingService(serviceRef);
             registerServlets();
             return httpService;
          }

          @Override
            public void removedService(ServiceReference ref, Object service) {
                if (httpService == service) {
                    unregisterServlets();
                    httpService = null;
                }
                super.removedService(ref, service);
            }
        };

        this.tracker.open();

        logger.info("HTTP SERVICE BUNDLE STARTED");
    }


    @Override
    public synchronized void stop(BundleContext bundleContext) throws Exception {
        this.tracker.close();
    }

    private void registerServlets() {
        try {
            rawRegisterServlets();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        } catch (ServletException se) {
            throw new RuntimeException(se);
        } catch (NamespaceException se) {
            throw new RuntimeException(se);
        }
    }

    private void rawRegisterServlets() throws ServletException, NamespaceException, InterruptedException {
        logger.info("JERSEY BUNDLE: REGISTERING SERVLETS");
        logger.info("JERSEY BUNDLE: HTTP SERVICE = " + httpService.toString());

        httpService.registerServlet("/jersey-http-service", new ServletContainer(), getJerseyServletParams(), null);
        httpService.registerServlet("/non-jersey-http-service", new SimpleNonJerseyServlet(), null, null);

        sendAdminEvent();
        logger.info("JERSEY BUNDLE: SERVLETS REGISTERED");
    }

    private void sendAdminEvent() {
        ServiceReference eaRef = bc.getServiceReference(EventAdmin.class.getName());
        if (eaRef != null) {
            EventAdmin ea = (EventAdmin) bc.getService(eaRef);
            ea.sendEvent(new Event("jersey/test/DEPLOYED", new HashMap<String, String>() {

                {
                    put("context-path", "/");
                }
            }));
            bc.ungetService(eaRef);
        }
    }

    private void unregisterServlets() {
        if (this.httpService != null) {
            logger.info("JERSEY BUNDLE: UNREGISTERING SERVLETS");
            httpService.unregister("/jersey-http-service");
            httpService.unregister("/non-jersey-http-service");
            logger.info("JERSEY BUNDLE: SERVLETS UNREGISTERED");
        }
    }

    private Dictionary<String, String> getJerseyServletParams() {
        Dictionary<String, String> jerseyServletParams = new Hashtable<String, String>();
        jerseyServletParams.put("javax.ws.rs.Application", JerseyApplication.class.getName());
        return jerseyServletParams;
    }

}
