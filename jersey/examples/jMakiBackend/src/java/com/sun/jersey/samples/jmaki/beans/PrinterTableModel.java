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
