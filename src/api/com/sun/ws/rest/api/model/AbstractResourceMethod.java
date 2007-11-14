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
 * AbstractResourceMethod.java
 *
 * Created on October 5, 2007, 11:46 AM
 *
 */

package com.sun.ws.rest.api.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * Abstraction for a resource method
 */
public class AbstractResourceMethod implements Parameterized {
    
    private List<MediaType> consumeMimeList;
    private List<MediaType> produceMimeList;
    private List<Parameter> parameters;
    private String httpMethod;
    private Method method;
    
    // TODO: what other methods to include?
    private enum HttpMETHOD {
        
        GET, POST, PUT, DELETE, HEAD;
                
        boolean isPrefixOf(final String what) {
            if (null == what) {
                return false;
            }
            return what.startsWith(this.name());
        }
    }
    
    
    private static String extractHttpMethodName(Method method) {

        final String upperCaseMethodName = method.getName().toUpperCase();

        for (HttpMETHOD httpMethod : HttpMETHOD.values()) {
            if (httpMethod.isPrefixOf(upperCaseMethodName)) {
                return httpMethod.name();
            }
        }
        // no http method found
        // TODO: shall we really return null?
        //       what e.g. a "NULL_HTTP_METHOD"
        return null;
    }
    
    
    /**
     * Creates a new instance of AbstractResourceMethod
     */
    public AbstractResourceMethod(Method method) {
        this(method, null);
    }

    public AbstractResourceMethod(Method method, String httpMethod) {
        this.method = method;
        if (null == httpMethod || "".equals(httpMethod)) {
            this.httpMethod = extractHttpMethodName(method);
        } else {
            this.httpMethod = httpMethod;
        }
        this.consumeMimeList = new ArrayList<MediaType>();
        this.produceMimeList = new ArrayList<MediaType>();
        this.parameters = new ArrayList<Parameter>();
    }
    
    public List<MediaType> getSupportedInputTypes() {
        return consumeMimeList;
    }

    public List<MediaType> getSupportedOutputTypes() {
        return produceMimeList;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Method getMethod() {
        return method;
    }
}
