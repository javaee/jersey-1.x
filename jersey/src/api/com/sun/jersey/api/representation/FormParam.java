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
package com.sun.jersey.api.representation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds the value(s) of a HTTP request representation form parameter to a 
 * resource method parameter, resource class field, or resource class bean 
 * property.
 * <p>
 * Values are URL decoded unless this is disabled using the {@link Encoded}
 * annotation. A default value can be specified using the {@link DefaultValue}
 * annotation.
 * <p>
 * This annotation is valid only on resource method signatures whose
 * parameters are all annotated with at least one parameter annotated with
 * this annotation.
 * <p>
 * The type <code>T</code> of the annotated parameter, field or property must 
 * either:
 * <ol>
 * <li>Be a primitive type</li>
 * <li>Have a constructor that accepts a single <code>String</code> argument</li>
 * <li>Have a static method named <code>valueOf</code> that accepts a single 
 * <code>String</code> argument (see, for example, {@link Integer#valueOf(String)})</li>
 * <li>Be <code>List&lt;T&gt;</code>, <code>Set&lt;T&gt;</code> or 
 * <code>SortedSet&lt;T&gt;</code>, where <code>T</code> satisfies 2 or 3 above.
 * The resulting collection is read-only.</li>
 * </ol>
 * 
 * <p>If the type is not one of those listed in 4 above then the first value 
 * (lexically) of the parameter is used.</p>
 *
 * <p>Use of this annotation on resource class fields and bean properties not
 * supported and will result in a runtime exception.</p>
 *
 * @see DefaultValue
 * @see Encoded
 * @see com.sun.jersey.api.representation.Form
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormParam {
    /**
     * Defines the name of the HTTP form parameter whose value will be used
     * to initialize the value of the annotated method argument, class field or
     * bean property.
     */
    String value();
    
    /**
     * Controls whether the the supplied form parameter name is URL encoded. 
     * If true, any characters in the query parameter name that are not valid
     * URI characters will be automatically encoded. If false then all 
     * characters in the supplied name must be valid URI characters.
     */
    boolean encode() default true;    
}