package com.sun.ws.rest.impl.container.grizzly.web;

import com.sun.ws.rest.api.core.DefaultResourceConfig;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that explicitly
 * references classes.
 * 
 * TODO move to the API.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ClassNameResourceConfig extends DefaultResourceConfig {

    public static final String PROPERTY_CLASSNAMES = "com.sun.ws.rest.config.property.classnames";

    public ClassNameResourceConfig(Class... classes) {
        super();
        
        for (Class c : classes)
            getResourceClasses().add(c);
    }
    
    public ClassNameResourceConfig(String[] names) {
        super(getClasses(names));
    }
    
    public ClassNameResourceConfig(Map<String, Object> props) {
        super(getClasses(props));
    }

    private static Set<Class<?>> getClasses(Map<String, Object> props) {
        Object v = props.get(PROPERTY_CLASSNAMES);
        if (v == null) {
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " property is missing");
        }
        Set<Class<?>> s = getClasses(v);
        if (s.isEmpty()) {
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " contains no paths");
        }
        return s;
    }

    private static Set<Class<?>> getClasses(Object param) {
        return convertToSet(_getClasses(param));
    }
    
    private static Set<Class<?>> getClasses(String[] elements) {
        return convertToSet(_getClasses(elements));
    }

    private static Set<Class<?>> convertToSet(String[] classes) {
        Set<Class<?>> s = new HashSet<Class<?>>();
        for (String c : classes) {
            try {
                s.add(getClassLoader().loadClass(c));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return s;        
    }
    
    private static String[] _getClasses(Object param) {
        if (param instanceof String) {
            return _getClasses((String) param);
        } else if (param instanceof String[]) {
            return _getClasses((String[]) param);
        } else {
            throw new IllegalArgumentException(PROPERTY_CLASSNAMES + " must " + "have a property value of type String or String[]");
        }
    }

    private static String[] _getClasses(String[] elements) {
        List<String> paths = new LinkedList<String>();
        for (String element : elements) {
            if (element == null || element.length() == 0) {
                continue;
            }
            Collections.addAll(paths, _getClasses(element));
        }
        return paths.toArray(new String[paths.size()]);
    }

    private static String[] _getClasses(String paths) {
        return paths.split(";");
    }
    
    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();        
        return (classLoader == null) ? ClassNameResourceConfig.class.getClassLoader() : classLoader;
    }    
}
