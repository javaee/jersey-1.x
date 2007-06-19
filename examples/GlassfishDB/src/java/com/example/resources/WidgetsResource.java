/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

/*
 * WidgetsResource.java
 *
 * Created on April 6, 2007, 5:08 PM
 *
 */

package com.example.resources;

import com.example.persistence.Widget;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;

@UriTemplate("/widgets")
public class WidgetsResource {
    
    @PersistenceUnit(unitName="WidgetPU")
    EntityManagerFactory emf;
    
    /** Creates a new instance of WidgetsResource */
    public WidgetsResource() {
    }
    
    @ProduceMime("text/plain")
    @HttpMethod("GET")
    public String getListOfWidgets() {
        StringBuilder b = new StringBuilder();
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            Query q = em.createQuery("SELECT w FROM Widget w");
            List<Widget> results = (List<Widget>)q.getResultList();
            for (Widget w: results) {
                b.append(w.getName()+": "+w.getDescription()+"\n");
            }
        } catch (Exception ex) {
            b.append(ex.toString());
        } finally {
            if (em != null)
                em.close();
        }
        return b.toString();
    }
}

