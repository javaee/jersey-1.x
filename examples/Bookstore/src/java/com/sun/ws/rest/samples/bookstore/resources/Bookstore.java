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

package com.sun.ws.rest.samples.bookstore.resources;

import com.sun.ws.rest.api.NotFoundException;
import com.sun.ws.rest.api.view.Views;
import com.sun.ws.rest.spi.resource.Singleton;
import java.util.Map;
import java.util.TreeMap;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/")
@Views({"index.jsp", "count.jsp", "time.jsp"})
@Singleton
public class Bookstore {    
    private final Map<String, Item> items = new TreeMap<String, Item>();
    
    private String name;
    
    public Bookstore() {
        setName("Czech Bookstore");
        getItems().put("1", new Book("Svejk", "Jaroslav Hasek"));
        getItems().put("2", new Book("Krakatit", "Karel Capek"));
        getItems().put("3", new CD("Ma Vlast 1", "Bedrich Smetana", new Track[]{
            new Track("Vysehrad",180),
            new Track("Vltava",172),
            new Track("Sarka",32)}));
    }
    
    @Path("items/{itemid}/")
    public Item getItem(@PathParam("itemid") String itemid) {
        Item i = getItems().get(itemid);
        if (i == null)
            throw new NotFoundException("Item, " + itemid + ", is not found");
        
        return i;
    }
    
    public long getSystemTime() {
        return System.currentTimeMillis();
    }

    public Map<String, Item> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
