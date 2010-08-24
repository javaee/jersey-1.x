/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.core.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;

/**
 * A utlity class for reading and writing using byte and character streams.
 * <p>
 * If a byte or character array is utilized then the size of the array
 * is by default the value of {@link #DEFAULT_BUFFER_SIZE}. This value can
 * be set using the system property {@link #BUFFER_SIZE_SYSTEM_PROPERTY}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ReaderWriter {
    /**
     * The UTF-8 Charset.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * The system property to set the default buffer size for arrays
     * of byte and character.
     * <p>
     * If the property value is not a positive integer then the default buffer
     * size declared by {@link #DEFAULT_BUFFER_SIZE} will be utilized.
     */
    public static final String BUFFER_SIZE_SYSTEM_PROPERTY =
            "com.sun.jersey.core.util.ReaderWriter.BufferSize";

    /**
     * The default buffer size for arrays of byte and character.
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    
    /**
     * The buffer size for arrays of byte and character.
     */
    public static final int BUFFER_SIZE = getBufferSize();


    private static int getBufferSize() {
        String v = System.getProperty(
                BUFFER_SIZE_SYSTEM_PROPERTY,
                Integer.toString(DEFAULT_BUFFER_SIZE));
        try {
            int i = Integer.valueOf(v);
            if (i <= 0)
                throw new NumberFormatException();
            return i;
        } catch (NumberFormatException ex) {
            return DEFAULT_BUFFER_SIZE;
        }
    }

    /**
     * Read bytes from an input stream and write them to an output stream.
     *
     * @param in the input stream to read from.
     * @param out the output stream to write to.
     * @throws IOException if there is an error reading or writing bytes.
     */
    public static final void writeTo(InputStream in, OutputStream out) throws IOException {
        int read;
        final byte[] data = new byte[BUFFER_SIZE];
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
    }

    /**
     * Read characters from an input stream and write them to an output stream.
     *
     * @param in the reader to read from.
     * @param out the writer to write to.
     * @throws IOException if there is an error reading or writing characters.
     */
    public static final void writeTo(Reader in, Writer out) throws IOException {
        int read;
        final char[] data = new char[BUFFER_SIZE];
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
    }

    /**
     * Get the character set from a media type.
     * <p>
     * The character set is obtained from the media type parameter "charset".
     * If the parameter is not present the {@link #UTF8} charset is utilized.
     *
     * @param m the media type.
     * @return the character set.
     */
    public static final Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get("charset");
        return (name == null) ? UTF8 : Charset.forName(name);
    }

    /**
     * Read the bytes of an input stream and convert to a string.
     *
     * @param in the input stream to read from.
     * @param type the media type that determines the character set defining
     *        how to decode bytes to charaters.
     * @return the string.
     * @throws IOException if there is an error reading from the input stream.
     */
    public static final String readFromAsString(InputStream in,
            MediaType type) throws IOException {
        return readFromAsString(new InputStreamReader(in, getCharset(type)));
    }

    /**
     * Read the characters of a reader and convert to a string.
     * 
     * @param reader the reader
     * @return the string
     * @throws IOException if there is an error reading from the reader.
     */
    public static final String readFromAsString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] c = new char[BUFFER_SIZE];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        }
        return sb.toString();
    }

    /**
     * Convert a string to bytes and write those bytes to an output stream.
     *
     * @param s the string to convert to bytes.
     * @param out the output stream to write to.
     * @param type the media type that determines the character set defining
     *        how to decode bytes to characters.
     * @throws IOException
     */
    public static final void writeToAsString(String s, OutputStream out,
            MediaType type) throws IOException {
        Writer osw = new BufferedWriter(new OutputStreamWriter(out,
                getCharset(type)));
        osw.write(s);
        osw.flush();
    }
}
