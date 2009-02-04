/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.core.header;

import com.sun.jersey.core.header.reader.HttpHeaderReader;
import java.io.DataInput;
import java.text.ParseException;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * An quality source media type.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class QualitySourceMediaType extends MediaType {

    public static final String QUALITY_SOURCE_FACTOR = "qs";

    public static final int DEFAULT_QUALITY_SOURCE_FACTOR = 1000;

    private final int qs;

    public QualitySourceMediaType(String p, String s) {
        super(p, s);
        qs = DEFAULT_QUALITY_SOURCE_FACTOR;
    }
    
    public QualitySourceMediaType(String p, String s, int qs, Map<String, String> parameters) {
        super(p, s, parameters);
        this.qs = qs;
    }
        
    public int getQualitySource() {
        return qs;
    }
    
    public static QualitySourceMediaType valueOf(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();

        // Get the type
        String type = reader.nextToken();
        reader.nextSeparator('/');
        // Get the subtype
        String subType = reader.nextToken();

        int qs = DEFAULT_QUALITY_SOURCE_FACTOR;
        Map<String, String> parameters = null;
        if (reader.hasNext()) {
            parameters = HttpHeaderReader.readParameters(reader);
            if (parameters != null) {
                String v = parameters.get(QUALITY_SOURCE_FACTOR);
                if (v != null) {
                    try {
                        qs = (int)(Float.valueOf(v) * 1000.0);
                    } catch (NumberFormatException ex) {
                        ParseException pe = new ParseException("The quality source (qs) value, " + v + ", is not a valid value", 0);
                        pe.initCause(ex);
                    }
                    if (qs < 0)
                        throw new ParseException("The quality source (qs) value, " + v + ", must be non-negative number", 0);
                }
            }
        }

        return new QualitySourceMediaType(type, subType, qs, parameters);
    }
}