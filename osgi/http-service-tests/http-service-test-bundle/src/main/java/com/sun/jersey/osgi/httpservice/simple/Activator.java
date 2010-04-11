package com.sun.jersey.osgi.httpservice.simple;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    private BundleContext bc;
    private HttpService httpService = null;
    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public synchronized void start(BundleContext bundleContext) throws Exception {
        this.bc = bundleContext;

        logger.info("STARTING HTTP SERVICE BUNDLE");
        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        registerServlets(bc);
                    }
                }).start();
        logger.info("HTTP SERVICE BUNDLE STARTED");
    }


    @Override
    public synchronized void stop(BundleContext bundleContext) throws Exception {

        if (this.httpService != null) {
                logger.info("JERSEY BUNDLE: UNREGISTERING SERVLETS");

                httpService.unregister("/jersey-http-service");
                httpService.unregister("/non-jersey-http-service");

                logger.info("JERSEY BUNDLE: SERVLETS UNREGISTERED");
        }
    }

    private void registerServlets(final BundleContext bundleContext) {
        try {
            rawRegisterServlets(bundleContext);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        } catch (ServletException se) {
            throw new RuntimeException(se);
        } catch (NamespaceException se) {
            throw new RuntimeException(se);
        }
    }

    private void rawRegisterServlets(final BundleContext bundleContext) throws ServletException, NamespaceException, InterruptedException {
        final ServiceTracker st = new ServiceTracker(bundleContext, HttpService.class.getName(), null);
        st.open();
        this.httpService = (HttpService) st.waitForService(0);

        logger.info("JERSEY BUNDLE: REGISTERING SERVLETS");
        logger.info("JERSEY BUNDLE: HTTP SERVICE = " + httpService.toString());

        httpService.registerServlet("/jersey-http-service", new ServletContainer(), getJerseyServletParams(), null);
        httpService.registerServlet("/non-jersey-http-service", new SimpleNonJerseyServlet(), null, null);

        logger.info("JERSEY BUNDLE: SERVLETS REGISTERED");
    }

    private Dictionary<String, String> getJerseyServletParams() {
        Dictionary<String, String> jerseyServletParams = new Hashtable<String, String>();
        jerseyServletParams.put("javax.ws.rs.Application", JerseyApplication.class.getName());
        return jerseyServletParams;
    }

}
