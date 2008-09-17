package com.sun.jersey.api.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Represents a generic type {@code T}.
 * 
 * @param <T> the generic type parameter.
 */
public class GenericType<T> {

    private final Type t;

    private final Class c;
    
    /**
     * Constructs a new generic type, deriving the generic type and class from
     * type parameter. Note that this constructor is protected, users should create
     * a (usually anonymous) subclass as shown above.
     *
     */
    protected GenericType() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        
        this.t = parameterized.getActualTypeArguments()[0];
        this.c = getClass(this.t);
    }
    
    /**
     * Constructs a new generic type, supplying the generic type
     * information and derving the class.
     *
     * @param genericType the generic type.
     * @throws IllegalArgumentException if genericType
     * is null or is neither an instance of Class or ParameterizedType whose raw
     * type is not an instance of Class.
     */
    public GenericType(Type genericType) {
        if (genericType == null) {
            throw new IllegalArgumentException("Type must not be null");
        }

        this.t = genericType;
        this.c = getClass(this.t);
    }
    
    private static Class getClass(Type type) {
        if (type instanceof Class) {
            return (Class)type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            if (parameterizedType.getRawType() instanceof Class) {
                return (Class)parameterizedType.getRawType();
            }
        }
        throw new IllegalArgumentException("Type parameter not a class or " +
                "parameterized type whose raw type is a class");        
    }
    
    /**
     * Gets underlying {@code Type} instance derived from the
     * type.
     * 
     * @return the type.
     */
    public final Type getType() {
        return t;
    }

    /**
     * Gets underlying raw class instance derived from the
     * type.
     * 
     * @return the class.
     */
    public final Class<T> getRawClass() {
        return c;
    }
}