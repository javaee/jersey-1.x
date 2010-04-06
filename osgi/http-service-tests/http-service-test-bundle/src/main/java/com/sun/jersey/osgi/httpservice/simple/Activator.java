package com.sun.jersey.osgi.httpservice.simple;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class Activator implements BundleActivator {

    private BundleContext bc;
    private boolean registered = false;
    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public synchronized void start(BundleContext bundleContext) throws Exception {
        this.bc = bundleContext;

        logger.info("STARTING HTTP SERVICE BUNDLE");

        ServiceListener sl = new ServiceListener() {

            @Override
            public void serviceChanged(ServiceEvent ev) {
                ServiceReference sr = ev.getServiceReference();
                switch (ev.getType()) {
                    case ServiceEvent.REGISTERED: {
                        HttpService http = (HttpService) bc.getService(sr);
                        Dictionary<String, String> jerseyServletParams = new Hashtable<String, String>();
                        jerseyServletParams.put("javax.ws.rs.Application", JerseyApplication.class.getName());
                        try {
                            if (!registered) {
                                logger.info("REGISTERING JERSEY SERVLET");
                                logger.info("HTTP SERVICE = " + http.toString());
                                logger.info("HTTP SERVICE CLASS NAME = " + http.getClass().getName());
                                Thread.dumpStack();
                                http.registerServlet("/non-jersey-http-service", new SimpleNonJerseyServlet(), null, null);
                                http.registerServlet("/jersey-http-service", new ServletContainer(), jerseyServletParams, null);
                                registered = true;
                            }
                        } catch (ServletException ex) {
                            Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NamespaceException ex) {
                            Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
        };

        String filter = "(objectclass=" + HttpService.class.getName() + ")";

        try {
            bundleContext.addServiceListener(sl, filter);
            ServiceReference[] srl = bundleContext.getServiceReferences(null, filter);
            for (int i = 0; srl != null && i < srl.length; i++) {
                sl.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, srl[i]));
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void stop(BundleContext bundleContext) throws Exception {
        // TODO: unregister the servlet
    }
}
