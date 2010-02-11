/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.server.osgi;

import com.sun.jersey.server.impl.provider.RuntimeDelegateImpl;
import javax.ws.rs.ext.RuntimeDelegate;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author japod
 */
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext bc) throws Exception {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        // TODO: what now brown cow?
    }

}
