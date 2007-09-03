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


package com.sun.ws.rest.samples.jsonfromjaxbtestclient;

import com.sun.ws.rest.samples.jsonfromjaxbtestclient.util.MyContentHandlerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author japod
 */
public class Main {

    static final String UrlBase = "http://localhost:8080/JsonFromJaxb/resources/";

    static final class HttpResponse {

        Object content;
        int code;
        String message;
    }

    static HttpResponse makeHttpRequest(String method, String url, Map<String, String> headers, InputStream is) throws Exception {
        HttpResponse response = new HttpResponse();
        URL urlAddress = new URL(url);
        HttpURLConnection huc = (HttpURLConnection) urlAddress.openConnection();
        huc.setRequestMethod(method);
        if (null != is) {
            huc.setDoOutput(true);
            for (String key : headers.keySet()) {
                huc.setRequestProperty(key, headers.get(key));
            }
            //huc.setRequestProperty("Content-Type", contentType);
            //huc.setRequestProperty("Accept", accept);
            OutputStream os = huc.getOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
        }
        response.code = huc.getResponseCode();
        response.message = huc.getResponseMessage();
        if (HttpURLConnection.HTTP_NO_CONTENT != response.code) {
            response.content = huc.getContent();
        }
        return response;
    }

    static HttpResponse makeHttpRequest(String method, String url, String contentType, String content) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", contentType);
        return makeHttpRequest(method, url, headers, new ByteArrayInputStream(content.getBytes("UTF-8")));
    }

    static HttpResponse makeHttpRequest(String method, String url) throws Exception {
        return makeHttpRequest(method, url, null, (InputStream) null);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        HttpURLConnection.setContentHandlerFactory(new MyContentHandlerFactory());
        
        Map<String, String> acceptXmlHeaders = new HashMap<String, String>();
        acceptXmlHeaders.put("Accept", "application/xml");
        
        HttpResponse response;

        try {
            System.out.println("Getting list of flights:");
            response = makeHttpRequest("GET", UrlBase + "flights/");
            System.out.println(new Formatter().format("List of flights found:\n%s", response.content));
            System.out.println("-----");
            
//            String updatedList = "{\"flights\":{\"flight\":[{\"@flightId\":\"OK125\",\"aircraft\":{\"$\":\"B737\"},\"company\":{\"$\":\"Czech Airlines\"},\"number\":{\"$\":\"125\"}},{\"@flightId\":\"OK126\",\"aircraft\":{\"$\":\"AB115\"},\"company\":{\"$\":\"Czech Airlines\"},\"number\":{\"$\":\"126\"}}]}}";
            String updatedListJson = "{\"flights\":{\"flight\":[{\"@flightId\":\"OK125\",\"aircraft\":{\"$\":\"B737\"},\"company\":{\"$\":\"Czech Airlines\"},\"number\":{\"$\":\"125\"}}]}}";
            System.out.println("Updating list of flights (JSON):");
            response = makeHttpRequest(
                    "PUT",
                    UrlBase + "flights/",
                    "application/json",
                    updatedListJson);
            System.out.println(new Formatter().format("Response code: %d \n%s", response.code, response.message));
            System.out.println("-----");

            System.out.println("Getting list of updated flights:");
            response = makeHttpRequest("GET", UrlBase + "flights/");
            System.out.println(new Formatter().format("List of flights found:\n%s", response.content));
            System.out.println("-----");
            
            String updatedListXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><flights><flight flightId=\"OK126\"><company>Czech Airlines</company><number>126</number><aircraft>B737</aircraft></flight></flights>";
            System.out.println("Updating list of flights (XML):");
            response = makeHttpRequest(
                    "PUT",
                    UrlBase + "flights/",
                    "application/xml",
                    updatedListXml);
            System.out.println(new Formatter().format("Response code: %d \n%s", response.code, response.message));
            System.out.println("-----");

            System.out.println("Getting list of updated flights:");
            response = makeHttpRequest("GET", UrlBase + "flights/", acceptXmlHeaders, null);
            System.out.println(new Formatter().format("List of flights found:\n%s", response.content));
            System.out.println("-----");
        } catch (Exception e) {
            System.out.println("TEST FAILED! :-(");
            e.printStackTrace(System.out);
        }
    }
}
