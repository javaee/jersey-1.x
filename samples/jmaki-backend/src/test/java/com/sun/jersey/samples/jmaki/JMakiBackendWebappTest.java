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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.samples.jmaki.beans.Printer;
import com.sun.jersey.samples.jmaki.beans.PrinterTableModel;
import com.sun.jersey.samples.jmaki.beans.TreeModel;
import com.sun.jersey.samples.jmaki.beans.WebResourceList;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;
import org.glassfish.embed.ScatteredWar;
import org.glassfish.embed.GlassFish;

/**
 *
 * @author Naresh
 */
public class JMakiBackendWebappTest extends TestCase {

     private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9998)).
                path("jMakiBackend/webresources").build();
    }

    private static final URI BASE_URI = getBaseURI();

    private GlassFish glassfish;

    private WebResource r;

    public JMakiBackendWebappTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Start Glassfish
        glassfish = new GlassFish(BASE_URI.getPort());
        // Deploy Glassfish referencing the web.xml
        ScatteredWar war = new ScatteredWar(BASE_URI.getRawPath(),
                new File("src/main/webapp"),
                new File("src/main/webapp/WEB-INF/web.xml"),
                Collections.singleton(new File("target/classes").toURI().toURL()));
        glassfish.deploy(war);
        Client c = Client.create();
        r = c.resource(BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        glassfish.stop();
    }

    /**
     * Test checks responses to requests to different resources.
     */
    public void testAll() {
        doTestApplicationWadl();
        doTestGetPrinters();
        doTestGetPrinterList();
        doTestGetPrinterJMakiTree();
        doTestGetPrinterJMakiTable();
        doTestGetPrinterBasedOnId();
        doTestPutPrinterBasedOnId();
    }

    /**
     * Test checks the application WADL is generated.
     */
    public void doTestApplicationWadl() {
        String wadl = r.path("application.wadl").accept(MediaTypes.WADL).get(String.class);
        assertTrue("Method: doTestApplicationWadl \nMessage: Something wrong, returned WADL length is not > 0",
                wadl.length() > 0);
    }

    /**
     * Test checks GET on resource "printers".
     */
    public void doTestGetPrinters() {
        // GET Printers - mime-type application/json
        WebResourceList resourceList = r.path("printers").accept(MediaType.APPLICATION_JSON)
                .get(WebResourceList.class);
        int numberOfResourceTypes = resourceList.items.size();
        assertEquals("Method: doTestGetPrinters \nMessage: Number of resource types retrieved " +
                "with MIME-TYPE application/json do not match the expected number.", 3, numberOfResourceTypes);

        // GET Printers - mime-type application/xml
        resourceList = r.path("printers").accept(MediaType.APPLICATION_XML)
                .get(WebResourceList.class);
        numberOfResourceTypes = resourceList.items.size();
        assertEquals("Method: doTestGetPrinters \nMessage: Number of resource types retrieved " +
                "with MIME-TYPE application/xml do not match the expected number.", 3, numberOfResourceTypes);
    }

    /**
     * Test checks GET on resource "printers/list".
     */
    public void doTestGetPrinterList() {
        //GET on printer list - mime-type application/json
        WebResourceList resourceList = r.path("printers").path("list").accept(MediaType.APPLICATION_JSON)
                .get(WebResourceList.class);
        int numberOfPrinters = resourceList.items.size();
        assertEquals("Method: doTestGetPrinterList \nMessage: Number of printers retrieved " +
                "with MIME-TYPE application/json do not match the expected number.", 5, numberOfPrinters);

        //GET on printer list - mime-type application/xml
        resourceList = r.path("printers").path("list").accept(MediaType.APPLICATION_XML)
                .get(WebResourceList.class);
        numberOfPrinters = resourceList.items.size();
        assertEquals("Method: doTestGetPrinterList \nMessage: Number of printers retrieved " +
                "with MIME-TYPE application/xml do not match the expected number.", 5, numberOfPrinters);
    }

    /**
     * Test checks GET on resource "printers/jMakiTree".
     */
    public void doTestGetPrinterJMakiTree() {
        //GET on printer list - mime-type application/json
        TreeModel treeModel = r.path("printers").path("jMakiTree")
                .accept(MediaType.APPLICATION_JSON).get(TreeModel.class);
        assertEquals("Method: doTestGetPrinterJMakiTree \nMessage: Root of the returned " +
                "jMakiTree doesn't match the expected value", "printers", treeModel.root.label);
    }

    /**
     * Test checks GET on resource "printers/jMakiTable".
     */
    public void doTestGetPrinterJMakiTable() {
        PrinterTableModel printerTableModel = r.path("printers").path("jMakiTable")
                .accept(MediaType.APPLICATION_JSON).get(PrinterTableModel.class);
        List<PrinterTableModel.JMakiTableHeader> tableHeaders = printerTableModel.columns;
        assertEquals("Method: doTestGetPrinterJMakiTable \nMessage: Number of table headers " +
                "do not match the expected number", 4, tableHeaders.size());
    }

    /**
     * Test checks GET on resource "printers/ids" based on id.
     */
    public void doTestGetPrinterBasedOnId() {
        Printer printer = r.path("printers").path("ids").path("P01")
                .accept(MediaType.APPLICATION_JSON).get(Printer.class);
        assertEquals("Method: doTestGetPrinterBasedOnId \nMessage: ID of the retrieved printer " +
                "doesn't match the search value", "P01", printer.id);
    }

    /**
     * Test checks PUT on resource "printers/ids" based on id.
     */
    public void doTestPutPrinterBasedOnId() {
        Printer printer = r.path("printers").path("ids").path("P01")
                .accept(MediaType.APPLICATION_JSON).get(Printer.class);
        String printerModel = printer.model;
        String printerLocation = printer.location;
        String printerUrl = printer.url;
        printer = new Printer("P01", "Xerox", printerLocation, printerUrl);
        ClientResponse response = r.path("printers").path("ids").path("P01")
                .type(MediaType.APPLICATION_JSON).put(ClientResponse.class, printer);
        assertEquals("Method: doTestPutPrinterBasedOnId \nMessage: Response status doesn't match the expected value.",
                Response.Status.NO_CONTENT, response.getResponseStatus());
        printer = r.path("printers").path("ids").path("P01")
                .accept(MediaType.APPLICATION_JSON).get(Printer.class);
        assertNotSame("Method: doTestPutPrinterBasedOnId \nMessage: Printer holds the old model inspite of update.", printerModel, printer.model);
        assertEquals("Method: doTestPutPrinterBasedOnId \nMessage: Updated printer model doesn't get reflected.", "Xerox", printer.model);
    }

}