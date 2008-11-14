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
package com.sun.jersey.spring25;

import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * An advice that logs around the execution of {@link ProxiedResource#getBaseUri()}.
 * Just for having some advice so that the {@link ProxiedResource} is proxied.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@Aspect
public class LoggingAdvice {
    
    private static final Logger LOGGER = Logger.getLogger( LoggingAdvice.class.getName() );
    
    @Around( "execution(* com.sun.jersey.spring25.ProxiedResource.getBaseUri(..))" )
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        final Signature signature = pjp.getSignature();
        LOGGER.info( "Starting to execute " + signature.getDeclaringTypeName() + "." + signature.getName() );
        Object retVal = pjp.proceed();
        LOGGER.info( "Finished to execute " + signature.getDeclaringTypeName() + "." + signature.getName() );
        return retVal;
      }
    
}
