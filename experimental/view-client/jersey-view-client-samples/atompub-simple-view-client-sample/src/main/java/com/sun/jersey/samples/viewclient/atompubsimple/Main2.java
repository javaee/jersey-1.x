/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.samples.viewclient.atompubsimple;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;
import com.sun.jersey.client.view.exception.FailedUserAssumptionException;
import com.sun.jersey.samples.viewclient.atompubsimple.provider.AtomEntryProvider;
import com.sun.jersey.samples.viewclient.atompubsimple.provider.AtomFeedProvider;
import com.sun.jersey.samples.viewclient.atompubsimple.provider.AtomServiceProvider;
import com.sun.jersey.samples.viewclient.atompubsimple.view.CollectionStateView;
import com.sun.jersey.samples.viewclient.atompubsimple.view.CreatedEntryView;
import com.sun.jersey.samples.viewclient.atompubsimple.view.EntryHandler;
import com.sun.jersey.samples.viewclient.atompubsimple.view.EntryStateView;
import com.sun.jersey.samples.viewclient.atompubsimple.view.ServiceStateView;
import org.apache.abdera.model.Entry;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class.
 *
 * @author algermissen@acm.org
 */
public class Main2 {

	public static final URI BASE_URI = UriBuilder.fromUri("http://localhost/").
			port(9998).build();
	public static final URI SERVICE_URI = UriBuilder.fromUri("http://localhost/service").
			port(9998).build();

	public static HttpServer startServer() throws IOException {
		final Map<String, String> initParams = new HashMap<String, String>();

		initParams.put("com.sun.jersey.config.property.packages",
				"com.sun.jersey.samples.viewclient.atompubsimple.server,com.sun.jersey.samples.viewclient.atompubsimple.provider");

		System.out.println("Starting grizzly...");
		return GrizzlyWebContainerFactory.create(
				BASE_URI, initParams);
	}

	public static void main(String[] args) throws Exception {
		// Grizzly initialization
		HttpServer httpServer = startServer();

		try {
			final Client client;
			ClientConfig config = new DefaultClientConfig();
			config.getClasses().add(AtomServiceProvider.class);
			config.getClasses().add(AtomFeedProvider.class);
			config.getClasses().add(AtomEntryProvider.class);
			client = Client.create(config);
			client.addFilter(new LoggingFilter());

			ServiceStateView ssv = client.view(SERVICE_URI,ServiceStateView.class);

			try {
				CreatedEntryView cev = ssv.createMediaEntry("text/plain","test data");
				System.out.println("New entry created at: " + cev.getLocation());

			} catch (FailedUserAssumptionException e) {
				System.out.println("Unable to create entry: " + e);
			}


			EntryStateView esv = client.view(
					BASE_URI.toASCIIString() + "entries/22",
					EntryStateView.class);


			esv.refresh();
			System.out.println("Entry view refreshed");

			CollectionStateView csv = client.view(
					BASE_URI.toASCIIString() + "collections/entries",
					CollectionStateView.class);

			csv.iterateEntries(new EntryHandler() {

				private int count = 0;

				public boolean handleEntry(Entry entry) {
					this.count++;
					System.out.println("ENTRY:" + entry.getTitle());

//					EntryStateView esv = client.view(entry.getLink("self").
//							getHref().
//							toASCIIString(), EntryStateView.class);
//
//					System.out.println("got view");
//
//
//
//					esv.update(esv.getEntry());
//					System.out.println("sent update");




					return (this.count <= 12);
				}
			});

			//CommonCatalogueView ccv = sv.getCommonCatalogue(ccv);
			

		} finally {
			httpServer.stop();
		}
	}
}

