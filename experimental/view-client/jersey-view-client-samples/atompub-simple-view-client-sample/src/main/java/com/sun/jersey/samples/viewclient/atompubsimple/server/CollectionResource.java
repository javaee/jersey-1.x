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
package com.sun.jersey.samples.viewclient.atompubsimple.server;

import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author algermissen@acm.org
 */
@Path("/collections")
public class CollectionResource {

	private static Abdera abdera = new Abdera();

	private static Abdera getAbdera() {
		return abdera;
	}

	@Path("entries")
	@GET
	@Produces("application/atom+xml")
	public Feed test(@DefaultValue("1") @QueryParam("p") int page,
			@Context UriInfo uriInfo) {

		URI nextUri = uriInfo.getAbsolutePathBuilder().replaceQueryParam("p",
				"" + (page + 1)).build();

		Feed feed = getAbdera().newFeed();
		feed.setTitle("Test Feed");
		for (int i = 1; i <= 10; i++) {
			int n = (page - 1) * 10 + i;
			feed.addEntry(this.makeEntry(uriInfo, n));
		}
		feed.addLink(nextUri.toASCIIString(), "next");
		return feed;
	}

	@Path("entries")
	@POST
	@Produces("application/atom+xml")
	@Consumes("application/atom+xml")
	public Response createEntry(Entry entryIn, @Context UriInfo uriInfo) {

		int n = 42; // arbitrary ID of new entry

		URI locationUri = uriInfo.getRequestUriBuilder().segment(String.valueOf(
				n)).build();
		Entry entryOut = this.makeEntry(uriInfo, n);
		entryOut.setTitle("Entry 21");
		entryOut.setContent("Text content of entry " + n);
		return Response.created(locationUri).entity(entryOut).build();
	}

	@Path("media")
	@POST
	@Produces("application/atom+xml")
	@Consumes("text/plain")
	public Response createMedia(String data, @Context UriInfo uriInfo) {

		URI locationUri = uriInfo.getRequestUriBuilder().segment("3554").build();
		Entry entry = getAbdera().newEntry();
		return Response.created(locationUri).entity(entry).build();
	}

	private Entry makeEntry(UriInfo uriInfo, int n) {
		Entry entry = getAbdera().newEntry();
		entry.setTitle("Entry " + n);
		entry.setContent("Text content of entry " + n);
		entry.addLink(uriInfo.getBaseUriBuilder().segment("entries").segment(
				"" + n).build().toASCIIString(), "edit");
		return entry;
	}
}
