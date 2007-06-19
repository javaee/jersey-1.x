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

package com.sun.ws.rest.impl.model.parameter;

import javax.ws.rs.DefaultValue;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.model.ReflectionHelper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MultivaluedDefaultListParameterProcessor {
    
    public static MultivaluedParameterExtractor process(Annotation[] parameterAnnotations, Class<?> parameter, Type parameterType, String parameterName) {
        String defaultValue = getDefaultValue(parameterAnnotations);
        
        if (parameter == List.class) {
            // Get the generic type of the list
            // If none default to String
            Class c = ReflectionHelper.getGenericClass(parameterType);
            if (c == null || c == String.class) {
                return new MultivaluedDefaultStringListExtractor(parameterName, defaultValue);
            } else {
                // Check for static valueOf(String )
                Method valueOf = ReflectionHelper.getValueOfStringMethod(c);
                if (valueOf != null) {
                    try {
                        return new MultivaluedDefaultValueOfListExtractor(valueOf, parameterName, defaultValue);
                    } catch (Exception e) {
                        throw new ContainerException(ImplMessages.DEFAULT_COULD_NOT_PROCESS_METHOD(defaultValue, valueOf));
                    }
                }

                // Check for constructor with String parameter
                Constructor constructor = ReflectionHelper.getStringConstructor(c);
                if (constructor != null) {
                    try {
                        return new MultivaluedDefaultStringConstructorListExtractor(constructor, parameterName, defaultValue);
                    } catch (Exception e) {
                        throw new ContainerException(ImplMessages.DEFAULT_COULD_NOT_PROCESS_CONSTRUCTOR(defaultValue, constructor));
                    }
                }
            }
        } else if (parameter == String.class) {
            return new MultivaluedDefaultStringExtractor(parameterName, defaultValue);            
        } else if (parameter.isPrimitive()) {
            // Convert primitive to wrapper class
            parameter = PrimitiveMapper.primitiveToClassMap.get(parameter);
            if (parameter == null) {
                // Primitive type not supported
                return null;
            }
            
            // Check for static valueOf(String )
            Method valueOf = ReflectionHelper.getValueOfStringMethod(parameter);
            if (valueOf != null) {
                try {
                    Object defaultDefaultValue = PrimitiveMapper.primitiveToDefaultValueMap.get(parameter);
                    return new MultivaluedDefaultPrimitiveValueOfExtractor(valueOf, parameterName, 
                            defaultValue, defaultDefaultValue);
                } catch (Exception e) {
                    throw new ContainerException(ImplMessages.DEFAULT_COULD_NOT_PROCESS_METHOD(defaultValue, valueOf));
                }
            }
        } else {
            // Check for static valueOf(String )
            Method valueOf = ReflectionHelper.getValueOfStringMethod(parameter);
            if (valueOf != null) {
                try {
                    return new MultivaluedDefaultValueOfExtractor(valueOf, parameterName, defaultValue);
                } catch (Exception e) {
                    throw new ContainerException(ImplMessages.DEFAULT_COULD_NOT_PROCESS_METHOD(defaultValue, valueOf));
                }
            }

            // Check for constructor with String parameter
            Constructor constructor = ReflectionHelper.getStringConstructor(parameter);
            if (constructor != null) {
                try {
                    return new MultivaluedDefaultStringConstructorExtractor(constructor, parameterName, defaultValue);
                } catch (Exception e) {
                    throw new ContainerException(ImplMessages.DEFAULT_COULD_NOT_PROCESS_CONSTRUCTOR(defaultValue, constructor));
                }
            }
        }
                
        return null;
    }

    private static String getDefaultValue(final Annotation[] parameterAnnotations) {
        DefaultValue dv = ReflectionHelper.getAnnotiationType(DefaultValue.class, parameterAnnotations);
        String defaultValue = null;
        if (dv != null) {
            defaultValue = dv.value();
            if (defaultValue.length() == 0)
                defaultValue = null;
        }
        return defaultValue;
    }    
}
