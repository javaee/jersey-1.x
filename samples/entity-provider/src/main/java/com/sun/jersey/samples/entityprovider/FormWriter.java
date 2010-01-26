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
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * FormWriter.java
 *
 * Created on November 21, 2007, 11:28 AM
 *
 */

package com.sun.jersey.samples.entityprovider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Hashtable;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author mh124079
 */
@Produces("text/html")
@Provider
public class FormWriter implements MessageBodyWriter<Hashtable<String, String>> {

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Hashtable.class.isAssignableFrom(type);
    }

    public long getSize(Hashtable<String,String> data, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return -1;
    }

    public void writeTo(Hashtable<String,String> data, 
            Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> headers, 
            OutputStream out) throws IOException {
        out.write(preamble.getBytes());
        for (String name: data.keySet()) {
            out.write("<tr><td>".getBytes());
            out.write(name.getBytes());
            out.write("</td><td>".getBytes());
            out.write(data.get(name).getBytes());
            out.write("</td></tr>".getBytes());            
        }
        out.write(postamble.getBytes());
    }
    
    private static String preamble = "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>\n" +
            "<html><head><title>Data</title></head>\n" +
            "<body><p>Add Data:</p>\n" +
            "<form name='pair' action='data' method='POST'>\n" +
            "<table>\n" +
            "   <tr>\n" +
            "       <td align='right'>Name:</td>\n" +
            "       <td><input type='text' name='name' value='' size='30' /></td>\n" +
            "   </tr>\n" +
            "   <tr>\n" +
            "       <td align='right'>Value:</td>\n" +
            "       <td><input type='text' name='value' value='' size='30' /></td>\n" +
            "   </tr>\n" +
            "   <tr><td></td><td><input type='submit' value='Set' name='submit' /></td></tr>\n" +
            "</table></form>\n" +
            "<p>Current Data:</p><table><tr><th>Name</th><th>Value</th></tr>\n";
    
    private static String postamble = "</table></body></html>";
}
