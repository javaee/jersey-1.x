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

package com.sun.jersey.samples.jmaki.beans;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author japod
 */
@XmlRootElement
public class PrinterTableModel {

    public static class JMakiTableHeader {

        public String id;
        public String label;

        public JMakiTableHeader() {
        }

        public JMakiTableHeader(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }
    
    public List<JMakiTableHeader> columns = initHeaders();
    public List<Printer> rows;
    
    public PrinterTableModel(){}
    
    public PrinterTableModel(Collection<Printer> rows) {
        this.rows = new LinkedList<Printer>();
        this.rows.addAll(rows);
    }
    
    static List<JMakiTableHeader> initHeaders() {
        List<JMakiTableHeader> headers = new LinkedList<JMakiTableHeader>();
        headers.add(new JMakiTableHeader("id", "Printer ID"));
        headers.add(new JMakiTableHeader("model", "Model"));
        headers.add(new JMakiTableHeader("url", "URL"));
        headers.add(new JMakiTableHeader("location", "Location"));
        return headers;
    }
}
