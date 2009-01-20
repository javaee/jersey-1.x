/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.json.impl.reader;

import java.io.StringWriter;
import junit.framework.TestCase;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;

/**
 *
 * @author japod
 */
public class JacksonRootAddingParserTest extends TestCase {

    public JacksonRootAddingParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNullJson() throws Exception {
        _testJsonExpr("null");
    }

    public void testJsonObject() throws Exception {
        _testJsonExpr("{\"one\":1}");
    }

    public void testJsonArray() throws Exception {
        _testJsonExpr("[\"one\",2,\"3\"]");
    }

    public void _testJsonExpr(String expr) throws Exception {
        JsonFactory factory = new JsonFactory();
        JsonParser p = factory.createJsonParser(expr);
        JsonParser rap = JacksonRootAddingParser.createRootAddingParser(p, "root");
        StringWriter sw = new StringWriter();
        JsonGenerator g = factory.createJsonGenerator(sw);
        rap.nextToken();
        while (rap.hasCurrentToken()) {
            g.copyCurrentEvent(rap);
            rap.nextToken();
        }
        g.flush();
        System.out.println(sw);
        assertEquals("{\"root\":" + expr + "}", sw.toString());
    }
}
