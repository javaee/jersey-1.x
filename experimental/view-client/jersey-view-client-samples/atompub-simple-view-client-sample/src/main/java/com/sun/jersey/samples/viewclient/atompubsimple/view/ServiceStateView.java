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
package com.sun.jersey.samples.viewclient.atompubsimple.view;

import com.sun.jersey.api.client.Client;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;

import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Service;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.client.view.exception.FailedUserAssumptionException;
import com.sun.jersey.client.view.exception.ResponseEntitySyntaxErrorException;
import javax.ws.rs.core.UriBuilder;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Workspace;

/**
 * @author algermissen@acm.org
 * 
 */
public class ServiceStateView {

	private Client client;
	private Service service;

	@GET
	@Consumes("application/atomsvc+xml")
	public void build(Service service, @Context Client c,
			@Context URI uri, @Context ClientResponse cr) {
		this.client = c;
		this.service = service;
		service.setBaseUri(new IRI(uri));
	}

	public Service getService() {
		return this.service;
	}

	public CollectionStateView getCollectionThatAcceptsUri(String mediaType) {
		List<Collection> cols = this.getService().getCollectionsThatAccept(
				mediaType);
		if (cols.isEmpty()) {
			return null;
		}

		CollectionStateView csv = client.view(cols.get(0).getHref().
				toASCIIString(), CollectionStateView.class);
		return csv;
	}

	public CreatedEntryView createEntry(String acceptedMediaType, Entry entry) {

		for (Workspace workspace : this.getService().getWorkspaces()) {
			Collection collection = workspace.getCollectionThatAccepts(
					acceptedMediaType);
			if (collection != null) {
				try {
					URI collectionUri = collection.getResolvedHref().toURI();
					ViewResource vr = this.client.viewResource(collectionUri);
					CreatedEntryView cev = vr.post(CreatedEntryView.class, entry);
					return cev;
				} catch (URISyntaxException ex) {
					throw new ResponseEntitySyntaxErrorException(ex);
				}
			}
		}
		throw new FailedUserAssumptionException(
				"No collection found that acepts " + acceptedMediaType);
	}

	public CreatedEntryView createMediaEntry(String acceptedMediaType,
			String data) {

		for (Workspace workspace : this.getService().getWorkspaces()) {
			Collection collection = workspace.getCollectionThatAccepts(
					acceptedMediaType);
			if (collection != null) {
				try {
					URI collectionUri = collection.getResolvedHref().toURI();
					ViewResource vr = this.client.viewResource(collectionUri);
					CreatedEntryView cev = vr.post(CreatedEntryView.class, data);
					return cev;
				} catch (URISyntaxException ex) {
					throw new ResponseEntitySyntaxErrorException(ex);
				}
			}
		}
		throw new FailedUserAssumptionException(
				"No collection found that acepts " + acceptedMediaType);
	}
}
