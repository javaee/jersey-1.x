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

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Apr 3, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public enum AutowireMode {
    
    AUTODETECT( AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT ),
    BY_NAME ( AutowireCapableBeanFactory.AUTOWIRE_BY_NAME ),
    BY_TYPE ( AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE ),
    CONSTRUCTOR ( AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR );
    
    private final int _springCode;
    
    private AutowireMode( int mode ) {
        _springCode = mode;
    }
    
    public int getSpringCode() {
        return _springCode;
    }
    
}
