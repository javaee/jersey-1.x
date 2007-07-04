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

package com.sun.ws.rest.samples.bookmark.test;

import com.sun.ws.rest.samples.bookmark.test.util.JSONContentHandlerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Formatter;
import org.json.JSONArray;

/**
 *
 * @author japod
 */
public class Main {
    
    final static String UrlBase = "http://localhost:8080/Bookmark/resources/";
    
    final static class HttpResponse {
        Object content;
        int code;
        String message;
    }
    
    static HttpResponse makeHttpRequest(String method, String url, String contentType, InputStream is) throws Exception {
        HttpResponse response = new HttpResponse();
        URL urlAddress = new URL(url);
        HttpURLConnection huc = (HttpURLConnection) urlAddress.openConnection();
        huc.setRequestMethod(method);
        if (null != is) {
            huc.setDoOutput(true);
            huc.setRequestProperty("Content-Type", contentType);
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
        return makeHttpRequest(method, url, contentType, new ByteArrayInputStream(content.getBytes("UTF-8")));
    }
    
    static HttpResponse makeHttpRequest(String method, String url) throws Exception {
        return makeHttpRequest(method, url, null, (InputStream)null);
    }
    
    public static void main(String[] args) {
        try {
            HttpURLConnection.setContentHandlerFactory(new JSONContentHandlerFactory());

            HttpResponse response;
            
            System.out.println("Getting list of users:");
            response = makeHttpRequest("GET", UrlBase + "users/");
            System.out.println(new Formatter().format("List of users found:\n%s", response.content));
            System.out.println("-----");
            
            System.out.println("Creating test user:");
            response = makeHttpRequest(
                    "PUT",
                    UrlBase + "users/testuid",
                    "application/json",
                    "{\"userid\":\"testuid\", \"password\":\"test\", \"email\":\"test@test.net\", \"username\":\"Test User\"}");
            System.out.println(new Formatter().format("Response code: %d \n%s", response.code, response.message));
            System.out.println("-----");
            
            // make sure it was added
            System.out.println("Getting list of users:");
            response = makeHttpRequest("GET", UrlBase + "users/");
            System.out.println(new Formatter().format("List of users found:\n%s", response.content));
            System.out.println("-----");
            
            System.out.println("Getting test user before update:");
            response = makeHttpRequest("GET", UrlBase + "users/testuid");
            System.out.println(new Formatter().format("User found:\n%s", response.content));
            System.out.println("-----");

            System.out.println("Updating test user:");
            response = makeHttpRequest(
                    "PUT",
                    UrlBase + "users/testuid",
                    "application/json",
                    "{\"userid\":\"testuid\", \"password\":\"TEST\", \"email\":\"new@email.net\", \"username\":\"Updated Test User\"}");
            System.out.println(new Formatter().format("Response code: %d \n%s", response.code, response.message));
            System.out.println("-----");
            
            // make sure test user was updated
            System.out.println("Getting test user:");
            response = makeHttpRequest("GET", UrlBase + "users/testuid");
            System.out.println(new Formatter().format("User found:\n%s", response.content));
            System.out.println("-----");

            System.out.println("Getting list of test user bookmarks:");
            response = makeHttpRequest("GET", UrlBase + "users/testuid/bookmarks");
            System.out.println(new Formatter().format("List of bookmarks found:\n%s", response.content));
            System.out.println("-----");

            System.out.println("Creating new bookmark for test user:");
            response = makeHttpRequest(
                    "POST",
                    UrlBase + "users/testuid/bookmarks",
                    "application/json",
                    "{\"uri\":\"http://java.sun.com/\", \"sdesc\":\"test desc\", \"ldesc\":\"long test description\"}");
            System.out.println(new Formatter().format("Response code: %d \n%s", response.code, response.message));
            System.out.println("-----");
            
            System.out.println("Getting list of test user bookmarks after one was added:");
            response = makeHttpRequest("GET", UrlBase + "users/testuid/bookmarks");
            System.out.println(new Formatter().format("List of bookmarks found:\n%s", response.content));
            System.out.println("-----");
            
            String testBookmarkUrl = ((JSONArray)response.content).getString(0);
            
            System.out.println("Getting bookmark details:");
            response = makeHttpRequest("GET", testBookmarkUrl);
            System.out.println(new Formatter().format("Bookmark details:\n%s", response.content));
            System.out.println("-----");

            System.out.println("Deleting test bookmark:");
            response = makeHttpRequest("DELETE", testBookmarkUrl);
            System.out.println(new Formatter().format("Response code: %d \n%s", response.code, response.message));
            System.out.println("-----");
            
            System.out.println("Getting list of test user bookmarks after test one was removed:");
            response = makeHttpRequest("GET", UrlBase + "users/testuid/bookmarks");
            System.out.println(new Formatter().format("List of bookmarks found:\n%s", response.content));
            System.out.println("-----");
            
            System.out.println("Deleting test user:");
            response = makeHttpRequest("DELETE", UrlBase + "users/testuid");
            System.out.println(new Formatter().format("Response code: %d \n%s", response.code, response.message));
            System.out.println("-----");
            
            System.out.println("Getting list of users:");
            response = makeHttpRequest("GET", UrlBase + "users/");
            System.out.println(new Formatter().format("List of users found:\n%s", response.content));
            System.out.println("-----");
            
        } catch (Exception e){
            System.out.println("TEST FAILED! :-(");
            e.printStackTrace(System.out);
        }
    }
}
