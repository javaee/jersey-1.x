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
package com.sun.jersey.api.spring;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * This annotation provides autowiring capabilities for users that use spring 2.0
 * but that want to get their beans autowired.
 * <p>
 * Autowiring is performed via {@link AutowireCapableBeanFactory#createBean(Class, int, boolean)}
 * to have a fully initialized bean, including applied BeanPostProcessors (in contrast to
 * {@link AutowireCapableBeanFactory#autowire()}).<br/>
 * The parameters <em>autowiring mode</em> and <em>dependencyCheck</em> when invoking
 * {@link AutowireCapableBeanFactory#createBean(Class, int, boolean)} are used as specified
 * with this annotation.
 * </p>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface Autowire {
    
    /**
     * The autowiring mode to use.
     * @return One of {@link AutowireMode}, {@link AutowireMode#AUTODETECT} by default.
     */
    AutowireMode mode() default AutowireMode.AUTODETECT;
    
    /**
     * Whether to perform a dependency check for objects (not applicable to autowiring a constructor, thus ignored there).
     * @return true or false, false by default.
     */
    boolean dependencyCheck() default false;
    
}
