/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License"). You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above. However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.samples.viewclient.atompubsimple.view;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.client.view.view.VoidView;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import org.apache.abdera.model.Entry;

/**
 * @author algermissen@acm.org
 * 
 */
public class EntryStateView {

	private Client client;
	private Entry entry;
	private ViewResource viewResource;

	@GET 
	@Consumes("application/atom+xml, application/atom+xml;type=entry")
	public void build(Entry entry, @Context Client c, @Context ViewResource viewResource,
		@Context ClientResponse cr) {
		this.client = c;
		this.viewResource = viewResource;
		this.entry = entry;
	}

	public void update(Entry newEntry) {
		VoidView v  = this.viewResource.header("Content-Type", "application/atom+xml").put(VoidView.class,newEntry);
	}

	public void update(String data,String contentType) {
		VoidView v  = this.viewResource.header("Content-Type", contentType).put(VoidView.class,data);
	}

	public void delete() {
		VoidView v = this.viewResource.delete(VoidView.class);
	}

	public Entry getEntry() {
		return this.entry;
	}

	public void refresh() {
		this.client.view(this.viewResource.getURI(), this);
	}
}
