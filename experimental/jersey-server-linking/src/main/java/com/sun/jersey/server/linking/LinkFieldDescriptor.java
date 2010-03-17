/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.jersey.server.linking;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Path;

/**
 * Utility class for working with {@link Link} annotated fields
 * @author mh124079
 */
public class LinkFieldDescriptor extends FieldDescriptor {

    private Link link;
    private Class<?> type;
    private Map<String, String> bindings;

    public LinkFieldDescriptor(Field f, Link l, Class<?> t) {
        super(f);
        link = l;
        type = t;
        bindings = new HashMap<String, String>();
        for (Binding binding: l.bindings()) {
            bindings.put(binding.name(), binding.value());
        }
    }
    
    public void setPropertyValue(Object instance, URI value) {
        setAccessibleField(f);
        try {
            f.set(instance, type.equals(URI.class) ? value : value.toString());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(LinkFieldDescriptor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LinkFieldDescriptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Link.Style getLinkStyle() {
        return link.style();
    }

    public String getLinkTemplate() {
        String template = null;
        if (!link.resource().equals(Class.class)) {
            // extract template from specified class' @Path annotation
            Path path = link.resource().getAnnotation(Path.class);
            template = path==null ? "" : path.value();
        } else {
            template = link.value();
        }
        return template;
    }

    public String getBinding(String name) {
        return bindings.get(name);
    }
}
