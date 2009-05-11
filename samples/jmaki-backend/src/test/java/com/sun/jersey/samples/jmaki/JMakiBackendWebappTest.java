/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.samples.jmaki;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.samples.jmaki.beans.Printer;
import com.sun.jersey.samples.jmaki.beans.PrinterTableModel;
import com.sun.jersey.samples.jmaki.beans.TreeModel;
import com.sun.jersey.samples.jmaki.beans.WebResourceList;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.util.ApplicationDescriptor;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Naresh
 */
public class JMakiBackendWebappTest extends JerseyTest {

    public JMakiBackendWebappTest() throws Exception {
        super();
        ApplicationDescriptor appDescriptor = new ApplicationDescriptor()
                .setContextPath("/jMakiBackend")
                .setServletPath("/webresources")
                .setRootResourcePackageName("com.sun.jersey.samples.jmaki");
        super.setupTestEnvironment(appDescriptor);
    }

    /**
     * Test checks the application WADL is generated.
     */
    @Test
    public void doTestApplicationWadl() {
        String wadl = webResource.path("application.wadl").accept(MediaTypes.WADL).get(String.class);
        Assert.assertTrue("Method: doTestApplicationWadl \nMessage: Something wrong, returned WADL length is not > 0",
                wadl.length() > 0);
    }

    /**
     * Test checks GET on resource "printers".
     */
    @Test
    public void doTestGetPrinters() {
        // GET Printers - mime-type application/json
        WebResourceList resourceList = webResource.path("printers").accept(MediaType.APPLICATION_JSON).get(WebResourceList.class);
        int numberOfResourceTypes = resourceList.items.size();
        Assert.assertEquals("Method: doTestGetPrinters \nMessage: Number of resource types retrieved " +
                "with MIME-TYPE application/json do not match the expected number.", 3, numberOfResourceTypes);

        // GET Printers - mime-type application/xml
        resourceList = webResource.path("printers").accept(MediaType.APPLICATION_XML).get(WebResourceList.class);
        numberOfResourceTypes = resourceList.items.size();
        Assert.assertEquals("Method: doTestGetPrinters \nMessage: Number of resource types retrieved " +
                "with MIME-TYPE application/xml do not match the expected number.", 3, numberOfResourceTypes);
    }

    /**
     * Test checks GET on resource "printers/list".
     */
    @Test
    public void doTestGetPrinterList() {
        //GET on printer list - mime-type application/json
        WebResourceList resourceList = webResource.path("printers").path("list").accept(MediaType.APPLICATION_JSON).get(WebResourceList.class);
        int numberOfPrinters = resourceList.items.size();
        Assert.assertEquals("Method: doTestGetPrinterList \nMessage: Number of printers retrieved " +
                "with MIME-TYPE application/json do not match the expected number.", 5, numberOfPrinters);

        //GET on printer list - mime-type application/xml
        resourceList = webResource.path("printers").path("list").accept(MediaType.APPLICATION_XML).get(WebResourceList.class);
        numberOfPrinters = resourceList.items.size();
        Assert.assertEquals("Method: doTestGetPrinterList \nMessage: Number of printers retrieved " +
                "with MIME-TYPE application/xml do not match the expected number.", 5, numberOfPrinters);
    }

    /**
     * Test checks GET on resource "printers/jMakiTree".
     */
    @Test
    public void doTestGetPrinterJMakiTree() {
        //GET on printer list - mime-type application/json
        TreeModel treeModel = webResource.path("printers").path("jMakiTree").accept(MediaType.APPLICATION_JSON).get(TreeModel.class);
        Assert.assertEquals("Method: doTestGetPrinterJMakiTree \nMessage: Root of the returned " +
                "jMakiTree doesn't match the expected value", "printers", treeModel.root.label);
    }

    /**
     * Test checks GET on resource "printers/jMakiTable".
     */
    @Test
    public void doTestGetPrinterJMakiTable() {
        PrinterTableModel printerTableModel = webResource.path("printers").path("jMakiTable").accept(MediaType.APPLICATION_JSON).get(PrinterTableModel.class);
        List<PrinterTableModel.JMakiTableHeader> tableHeaders = printerTableModel.columns;
        Assert.assertEquals("Method: doTestGetPrinterJMakiTable \nMessage: Number of table headers " +
                "do not match the expected number", 4, tableHeaders.size());
    }

    /**
     * Test checks GET on resource "printers/ids" based on id.
     */
    @Test
    public void doTestGetPrinterBasedOnId() {
        Printer printer = webResource.path("printers").path("ids").path("P01").accept(MediaType.APPLICATION_JSON).get(Printer.class);
        Assert.assertEquals("Method: doTestGetPrinterBasedOnId \nMessage: ID of the retrieved printer " +
                "doesn't match the search value", "P01", printer.id);
    }

    /**
     * Test checks PUT on resource "printers/ids" based on id.
     */
    @Test
    public void doTestPutPrinterBasedOnId() {
        LoggingFilter loggingFilter = new LoggingFilter();
        webResource.addFilter(loggingFilter);
        Printer printer = webResource.path("printers").path("ids").path("P01").accept(MediaType.APPLICATION_JSON).get(Printer.class);
        String printerModel = printer.model;
        String printerLocation = printer.location;
        String printerUrl = printer.url;
        printer = new Printer("P01", "Xerox", printerLocation, printerUrl);
        ClientResponse response = webResource.path("printers").path("ids").path("P01").type(MediaType.APPLICATION_JSON).put(ClientResponse.class, printer);
        Assert.assertEquals("Method: doTestPutPrinterBasedOnId \nMessage: Response status doesn't match the expected value.",
                Response.Status.NO_CONTENT, response.getResponseStatus());
        printer = webResource.path("printers").path("ids").path("P01").accept(MediaType.APPLICATION_JSON).get(Printer.class);
        Assert.assertNotSame("Method: doTestPutPrinterBasedOnId \nMessage: Printer holds the old model inspite of update.", printerModel, printer.model);
        Assert.assertEquals("Method: doTestPutPrinterBasedOnId \nMessage: Updated printer model doesn't get reflected.", "Xerox", printer.model);
    }
}