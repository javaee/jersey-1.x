package com.sun.jersey.osgi.tests;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Activator implements BundleActivator {

	private GrizzlyWebServer gws;

	@Override
	public void start(BundleContext context) throws Exception {
		try {

			System.out.println("Activator.start()");

			gws = new GrizzlyWebServer(8080);

			ServletAdapter jerseyAdapter = new ServletAdapter();
			jerseyAdapter.addInitParameter("com.sun.jersey.config.property.classnames",
					SimpleResource.class.getCanonicalName());
			jerseyAdapter.addInitParameter("com.sun.jersey.config.property.resourceConfigClass",
					ClassNamesResourceConfig.class.getName());
			jerseyAdapter.setContextPath("/jersey");
			jerseyAdapter.setServletInstance(new ServletContainer());

			gws.addGrizzlyAdapter(jerseyAdapter, new String[] { "/jersey" });

                        System.out.println("STARTING JERSEY!!!");

			gws.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("STOPPING JERSEY!!!");
		gws.stop();
	}

}
