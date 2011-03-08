/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at http://jersey.java.net/CDDL+GPL.html
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
package com.sun.jersey.qe.tests.monitoring.commons;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author naresh
 */
public class ResourceMonitor {

    private WebResource monitoringResource;

    private String applicationName;

    private String glassFishHome;

    private String asadminCommand = "asadmin";

    public ResourceMonitor(String applicationName) {
        URI monitoringUri = UriBuilder
                .fromUri("http://localhost/monitoring/domain/server/applications/" +
                applicationName +
                "/jersey/resources")
                .port(getAdminPort(4848))
                .build();
        Client client = Client.create();
        monitoringResource = client.resource(monitoringUri);
        monitoringResource.addFilter(new LoggingFilter());
        this.applicationName = applicationName;
    }

    public ResourceMonitor(String applicationName, String glassFishHome) {
        this(applicationName);
        this.glassFishHome = glassFishHome;
        this.asadminCommand = glassFishHome + System.getProperty("file.separator")
                + "bin" + System.getProperty("file.separator") + asadminCommand;
    }

    private int getAdminPort(int adminPort) {
        return ((System.getProperty("JERSEY_ADMIN_PORT") != null)
                ? Integer.parseInt(System.getProperty("JERSEY_ADMIN_PORT"))
                : adminPort);
    }

    public String getResourceMonitoredValue(String monitorAttribute) {
        String monitoredData = monitoringResource
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        JSONObject jSONObject;
        try {
            jSONObject = new JSONObject(monitoredData);
            JSONObject resourceJSON = jSONObject.getJSONObject("Resources");
            return resourceJSON.getString(monitorAttribute);
        } catch (JSONException ex) {
           ex.printStackTrace();
        }
        return "";
    }

    public String getResourceHitCountValue(String hitcountcategory) {
        String monitoredData = monitoringResource
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        JSONObject jSONObject;
        try {
            jSONObject = new JSONObject(monitoredData);
            JSONObject resourceJSON = jSONObject.getJSONObject("Resources");
            resourceJSON = resourceJSON.getJSONObject(hitcountcategory);
            return resourceJSON.toString();
        } catch (JSONException ex) {
            
        }
        return "";
    }

    public int getPerResourceHitCountValue(String hitcountcategory,
            String resourceClassFullyQualifiedName) {
        String monitoredData = monitoringResource
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        JSONObject jSONObject;
        try {
            jSONObject = new JSONObject(monitoredData);
            JSONObject resourceJSON = jSONObject.getJSONObject("Resources");
            resourceJSON = resourceJSON.getJSONObject(hitcountcategory);
            return resourceJSON.getInt(resourceClassFullyQualifiedName);
        } catch (JSONException ex) {
            
        }
        return 0;
    }

    public int getResourceMonitoredIntValueFromAdminCLI(String monitorAttribute,
            String resourceClassName) {
        String value = getResourceMonitoredValueFromAdminCLI(monitorAttribute, resourceClassName);
        System.out.println("ATTRIBUTE VALUE ::#" + value + "#");
        return (value == null || value.equals("")) ? 0 : Integer.parseInt(value);
    }

    public String getResourceMonitoredValueFromAdminCLI(String monitorAttribute,
            String resourceClassName) {
        String resourceIdentifier = getResourceIdentifierForResource(resourceClassName);

        System.out.println("JERSEY RESOURCE ID :: " + resourceIdentifier);

        if (resourceIdentifier != null) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(asadminCommand).append(" --port=")
                    .append(getAdminPort(4848))
                    .append(" get -m server.*");
                Process process = Runtime.getRuntime().exec(stringBuilder.toString());
                try {
                    System.out.println("asadmin get status :: " + process.waitFor());
                } catch (InterruptedException ex) {
                    Logger.getLogger(ResourceMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Process process = Runtime.getRuntime().exec(stringBuilder.toString());
                InputStream inputStream = process.getInputStream();

                BufferedReader in = new BufferedReader(
                                     new InputStreamReader(inputStream));
                String line = null;
                StringBuilder monitoredData = new StringBuilder();
                while ((line = in.readLine()) != null) {
                     if(line.contains(applicationName)
                             && line.contains(resourceIdentifier)
                             && line.contains("."+monitorAttribute)) {
                         System.out.println(line);
                         monitoredData.append(line.split("=")[1].trim());
                         break;
                     }
                }

                return monitoredData.toString();
            } catch (IOException ex) {
                Logger.getLogger(ResourceMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private String getResourceIdentifierForResource(String resourceClassName) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(asadminCommand).append(" --port=")
                .append(getAdminPort(4848))
                .append(" get -m server.*");

            Process process = Runtime.getRuntime().exec(stringBuilder.toString());
            try {
                System.out.println("asadmin get status :: " + process.waitFor());
            } catch (InterruptedException ex) {
                Logger.getLogger(ResourceMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputStream inputStream = process.getInputStream();

            BufferedReader in = new BufferedReader(
                                 new InputStreamReader(inputStream));
             String line = null;
             StringBuilder monitoredData = new StringBuilder();
             while ((line = in.readLine()) != null) {
                 if(line.contains(applicationName) && line.contains(resourceClassName)) {
                     System.out.println(line);
                     monitoredData.append(line.split("=")[0].trim());
                     break;
                 }
             }

             int jerseyResourcesIndex = monitoredData.indexOf("jersey.resources");
             // read the index of the "." which precedes the resource identifier
             int resourceIdStartIndex = monitoredData.indexOf(".", jerseyResourcesIndex + 7);
             // read the index of the "." which follows the resource identifier
             int resourceIdEndIndex = monitoredData.indexOf(".", resourceIdStartIndex + 1);

             

             System.out.println("RESOURCE IDENTIFIER READS :: " + monitoredData);
             System.out.print("RESOURCE IDENTIFIER INDICES :: " + resourceIdStartIndex + "  and " + resourceIdEndIndex);

             if (jerseyResourcesIndex == -1) {
                 return null;
             }

            return monitoredData.substring(resourceIdStartIndex + 1, resourceIdEndIndex);
        } catch (IOException ex) {
            Logger.getLogger(ResourceMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}