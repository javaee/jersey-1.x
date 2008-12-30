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

package com.sun.jersey.multipart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Proxy class representing the entity of a {@link BodyPart} when a
 * {@link MultiPart} entity is received and parsed.  Its primary purpose
 * is to provide an input stream to retrieve the actual data.  However, it
 * also transparently deals with storing the data in a temporary disk file,
 * if it is larger than a configurable size; otherwise, the data is stored
 * in memory for faster processing.</p>
 */
public class BodyPartEntity {


    /**
     * <p>Default threshold size if not specified to our constructor.</p>
     */
    private static final int DEFAULT_THRESHOLD = 4096;


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new {@link BodyPartEntity} with a default threshold size.</p>
     *
     * @param stream <code>InputStream</code> containing the raw bytes of
     *  this body part
     *
     * @exception IOException if an input/output error occurs
     */
    public BodyPartEntity(InputStream stream) throws IOException {
        this(stream, DEFAULT_THRESHOLD);
    }


    /**
     * <p>Construct a new {@link BodyPartEntity} with a specified threshold size.</p>
     *
     * @param stream <code>InputStream</code> containing the raw bytes of
     *  this body part
     * @param threshold Desired threshold size
     *
     * @exception IllegalArgumentException if the specified threshold is
     *  not positive
     * @exception IOException if an input/output error occurs
     */
    public BodyPartEntity(InputStream stream, int threshold) throws IOException {
        // Validate and set our threshold
        if (threshold <= 0) {
            throw new IllegalArgumentException("Invalid threshold value " + threshold);
        }
        this.threshold = threshold;
        // Absorb the bytes from the request or response
        byte buffer[] = new byte[threshold];
        int n = stream.read(buffer);
        if (n < 0) { // Zero length entity
            data = new byte[0];
        } else if (n < threshold) {
            data = new byte[n];
            System.arraycopy(buffer, 0, data, 0, n);
        } else {
            file = File.createTempFile("BodyPartEntity.", ".data");
            OutputStream ostream = new BufferedOutputStream(new FileOutputStream(file), threshold);
            ostream.write(buffer, 0, n);
            while (true) {
                n = stream.read(buffer);
                if (n <= 0) {
                    break;
                }
                ostream.write(buffer, 0, n);
            }
            ostream.close();
        }
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Byte buffer containing our raw bytes, if smaller than the specified
     * threshold.  If the data is larger than the threshold, this value should
     * be set to <code>null</code>.</p>
     */
    private byte data[] = null;


    /**
     * <p>File containing our raw bytes, if larger than the specified
     * threshold.  If the data is smaller than the threshold, this value should
     * be set to <code>null</code>.</p>
     */
    private File file = null;


    /**
     * <p>Opened input stream (if any) returned by our <code>getInputStream()</code>
     * method.</p>
     */
    private InputStream istream = null;


    /**
     * <p>Threshold size (in bytes) over which the raw data for this body part
     * will be stored in a temporary disk file, instead of in memory.</p>
     */
    private int threshold = 0;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Clean up the temporary file we used, if any.</p>
     */
    public void cleanup() {
        if (istream != null) {
            try {
                istream.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        istream = null;
        if ((file != null) && file.exists()) {
            file.delete();
        }
    }


    /**
     * <p>Return an input stream to the raw bytes of this body part entity.</p>
     */
    public InputStream getInputStream() throws IOException {
        if (data != null) {
            istream = new ByteArrayInputStream(data);
        } else {
            istream = new BufferedInputStream(new FileInputStream(file), threshold);
        }
        return istream;
    }


}
