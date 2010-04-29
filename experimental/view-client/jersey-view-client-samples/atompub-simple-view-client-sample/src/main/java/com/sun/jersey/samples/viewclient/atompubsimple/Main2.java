/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License"). ÊYou
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt. ÊSee the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. ÊIf applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." ÊIf you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above. ÊHowever, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.samples.viewclient.atompubsimple;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
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

	public static SelectorThread startServer() throws IOException {
		final Map<String, String> initParams = new HashMap<String, String>();

		initParams.put("com.sun.jersey.config.property.packages",
				"com.sun.jersey.samples.viewclient.atompubsimple.server,com.sun.jersey.samples.viewclient.atompubsimple.provider");

		System.out.println("Starting grizzly...");
		SelectorThread threadSelector = GrizzlyWebContainerFactory.create(
				BASE_URI, initParams);
		return threadSelector;
	}

	public static void main(String[] args) throws Exception {
		// Grizzly initialization
		SelectorThread threadSelector = startServer();

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
			threadSelector.stopEndpoint();
		}
	}
}

