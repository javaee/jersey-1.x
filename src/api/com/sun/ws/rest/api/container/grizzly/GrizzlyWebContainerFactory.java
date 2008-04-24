package com.sun.ws.rest.api.container.grizzly;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ClasspathResourceConfig;
import com.sun.ws.rest.spi.container.servlet.ServletContainer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.servlet.Servlet;

/**
 * Factory for creating and starting Grizzly {@link SelectorThread} instances
 * for deploying a Servlet.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class GrizzlyWebContainerFactory {
    
    private GrizzlyWebContainerFactory() {}
    
    public static SelectorThread create(String u) throws IOException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u));
    }
    
    public static SelectorThread create(String u, Map<String, String> initParams) throws IOException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");
        
        return create(URI.create(u), initParams);
    }
    
    public static SelectorThread create(URI u) throws IOException {
        return create(u, ServletContainer.class);
    }
        
    public static SelectorThread create(URI u, 
            Map<String, String> initParams) throws IOException {
        return create(u, ServletContainer.class, initParams);
    }
    
    public static SelectorThread create(String u, Class<? extends Servlet> c) throws IOException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), c);
    }
    
    public static SelectorThread create(String u, Class<? extends Servlet> c,
            Map<String, String> initParams) throws IOException {
        if (u == null)
            throw new IllegalArgumentException("The URI must not be null");

        return create(URI.create(u), c, initParams);
    }
    
    public static SelectorThread create(URI u, Class<? extends Servlet> c) throws IOException {
        return create(u, c, null);
    }
    
    public static SelectorThread create(URI u, Class<? extends Servlet> c, 
            Map<String, String> initParams) throws IOException {
        
        ServletAdapter adapter = new ServletAdapter();
        if (initParams == null) {
            adapter.addInitParameter(ClasspathResourceConfig.PROPERTY_CLASSPATH, 
                     System.getProperty("java.class.path").replace(File.pathSeparatorChar, ';'));
        } else {
            for (Map.Entry<String, String> e : initParams.entrySet()) {
                adapter.addInitParameter(e.getKey(), e.getValue());
            }
        }
        
        adapter.setServletInstance(getInstance(c));
        
        String path = u.getPath();
        if (path == null)
            throw new IllegalArgumentException("The URI path, of the URI " + u + 
                    ", must be non-null");
        else if (path.length() == 0)
            throw new IllegalArgumentException("The URI path, of the URI " + u + 
                    ", must be present");
        else if (path.charAt(0) != '/')
            throw new IllegalArgumentException("The URI path, of the URI " + u + 
                    ". must start with a '/'");
        
        if (path.length() > 1) {
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);
            adapter.setContextPath(path);
        }
        
        return GrizzlyServerFactory.create(u, adapter);
    }    
    
     private static Servlet getInstance(Class<? extends Servlet> c){        
         try{                              
             return c.newInstance();
         } catch (Exception e) {
             throw new ContainerException(e);
         }   
     }     
}