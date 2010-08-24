/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.spring.jerseymanaged;

import com.sun.jersey.spring.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.resource.Singleton;
import junit.framework.Assert;

/**
 * A resource class that injects two spring beans of the same type using the
 * @Inject.value to differentiate between the beans.<br>
 * Created on: Jun 19., 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Path(NameBasedInjectingResource.PATH)
@Singleton
public class NameBasedInjectingResource {

    static final String PATH = "NameBasedInjectingResource";

    @Inject("namedItem3_1")
    private Item3 _item1;

    @InjectParam("namedItem3_2")
    private Item3 _item2;

    public NameBasedInjectingResource() {
    }

    @GET
    @Path("{value}")
    @Produces("application/xml")
    public Item3 getItemByValue(
            @PathParam("value") String value,
            @Inject("namedItem3_1") Item3 item1,
            @InjectParam("namedItem3_2") Item3 item2) {

        Assert.assertEquals(item1, _item1);
        Assert.assertEquals(item2, _item2);
        
        if (value.equals(_item1.getValue())) {
            return _item1;
        } else if (value.equals(_item2.getValue())) {
            return _item2;
        } else {
            throw new NotFoundException("No item with value '" + value + "' found.");
        }
    }
}
