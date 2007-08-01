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

package com.sun.ws.rest.impl.model;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * List of {@link MediaType}.
 *
 * TODO remove this class.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MediaTypeList extends ArrayList<MediaType> {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (o == null)
            return false;
        
        if (!(o instanceof MediaTypeList))
            return false;
        
        MediaTypeList that = (MediaTypeList)o;
        
        if (this.size() != that.size())
            return false;
        
        for (int i = 0; i < this.size(); i++)
            if (!this.get(i).equals(that.get(i)))
                return false;
            
         return true;
    }
}