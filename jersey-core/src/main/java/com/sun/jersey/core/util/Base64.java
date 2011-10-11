/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2010 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.jersey.core.util;

/**
 * This class provides encode/decode for RFC 2045 Base64 as defined by
 * RFC 2045, N. Freed and N. Borenstein.  <a
 * href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>:
 * Multipurpose Internet Mail Extensions (MIME) Part One: Format of
 * Internet Message Bodies. Reference 1996
 *
 * @author Jeffrey Rodriguez
 */
public final class  Base64
{
    static private final int  BASELENGTH         = 256;
    static private final int  LOOKUPLENGTH       = 64;
    static private final int  TWENTYFOURBITGROUP = 24;
    static private final int  EIGHTBIT           = 8;
    static private final int  SIXTEENBIT         = 16;
    static private final int  FOURBYTE           = 4;
    static private final int  SIGN               = -128;
    static private final byte PAD                = (byte) '=';
    static private byte [] base64Alphabet       = new byte[BASELENGTH];
    static private byte [] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];

    static
    {
        for (int i = 0; i < BASELENGTH; i++)
        {
            base64Alphabet[i] = -1;
        }
        for (int i = 'Z'; i >= 'A'; i--)
        {
            base64Alphabet[i] = (byte) (i - 'A');
        }
        for (int i = 'z'; i>= 'a'; i--)
        {
            base64Alphabet[i] = (byte) (i - 'a' + 26);
        }
        for (int i = '9'; i >= '0'; i--)
        {
            base64Alphabet[i] = (byte) (i - '0' + 52);
        }

        base64Alphabet['+']  = 62;
        base64Alphabet['/']  = 63;

        for (int i = 0; i <= 25; i++ )
            lookUpBase64Alphabet[i] = (byte) ('A' + i);

        for (int i = 26,  j = 0; i <= 51; i++, j++ )
            lookUpBase64Alphabet[i] = (byte) ('a'+ j);

        for (int i = 52,  j = 0; i <= 61; i++, j++ )
            lookUpBase64Alphabet[i] = (byte) ('0' + j);

        lookUpBase64Alphabet[62] = (byte) '+';
        lookUpBase64Alphabet[63] = (byte) '/';
    }

    public static boolean isBase64(String isValidString)
    {
        return isArrayByteBase64(isValidString.getBytes());
    }

    public static boolean isBase64( byte octet )
    {
        //shall we ignore white space? JEFF??
        return (octet == PAD || base64Alphabet[(octet & 0x0F) + (octet & 0xF0)] != -1);
    }

    public static boolean isArrayByteBase64(byte[] arrayOctect)
    {
        int length = arrayOctect.length;
        if (length == 0)
        {
            // shouldn't a 0 length array be valid base64 data?
            // return false;
            return true;
        }
        for (int i=0; i < length; i++)
        {
            if ( !Base64.isBase64(arrayOctect[i]) )
                return false;
        }
        return true;
    }

    /**
     * Encodes hex octects into Base64.
     *
     * @param binaryData Array containing binary data to encode.
     * @return Base64-encoded data.
     */
    public static byte[] encode( byte[] binaryData )
    {
        int      lengthDataBits    = binaryData.length*EIGHTBIT;
        int      fewerThan24bits   = lengthDataBits%TWENTYFOURBITGROUP;
        int      numberTriplets    = lengthDataBits/TWENTYFOURBITGROUP;
        byte     encodedData[];


        if (fewerThan24bits != 0)
        {
            //data not divisible by 24 bit
            encodedData = new byte[ (numberTriplets + 1 ) * 4 ];
        }
        else
        {
            // 16 or 8 bit
            encodedData = new byte[ numberTriplets * 4 ];
        }

        byte k, l, b1, b2, b3;

        int encodedIndex;
        int dataIndex;
        int i;
        for ( i = 0; i<numberTriplets; i++ )
        {
            dataIndex = i*3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            b3 = binaryData[dataIndex + 2];

            l  = (byte)(b2 & 0x0f);
            k  = (byte)(b1 & 0x03);

            encodedIndex = i * 4;
            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);
            byte val2 = ((b2 & SIGN)==0)?(byte)(b2>>4):(byte)((b2)>>4^0xf0);
            byte val3 = ((b3 & SIGN)==0)?(byte)(b3>>6):(byte)((b3)>>6^0xfc);

            encodedData[encodedIndex]   = lookUpBase64Alphabet[ val1 ];
            encodedData[encodedIndex+1] =
                    lookUpBase64Alphabet[ val2 | ( k<<4 )];
            encodedData[encodedIndex+2] =
                    lookUpBase64Alphabet[ (l <<2 ) | val3 ];
            encodedData[encodedIndex+3] = lookUpBase64Alphabet[ b3 & 0x3f ];
        }

        // form integral number of 6-bit groups
        dataIndex    = i*3;
        encodedIndex = i*4;
        if (fewerThan24bits == EIGHTBIT )
        {
            b1 = binaryData[dataIndex];
            k = (byte) ( b1 &0x03 );
            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);
            encodedData[encodedIndex]     = lookUpBase64Alphabet[ val1 ];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[ k<<4 ];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        }
        else if (fewerThan24bits == SIXTEENBIT)
        {

            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex +1 ];
            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            byte val1 = ((b1 & SIGN) == 0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);
            byte val2 = ((b2 & SIGN) == 0)?(byte)(b2>>4):(byte)((b2)>>4^0xf0);

            encodedData[encodedIndex]     = lookUpBase64Alphabet[ val1 ];
            encodedData[encodedIndex + 1] =
                    lookUpBase64Alphabet[ val2 | ( k<<4 )];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[ l<<2 ];
            encodedData[encodedIndex + 3] = PAD;
        }

        return encodedData;
    }

    /**
     * Decodes Base64 data into octects
     *
     * @param base64Data Byte array containing Base64 data
     * @return Array containing decoded data.
     */
    public static byte[] decode( byte[] base64Data )
    {
        // handle the edge case, so we don't have to worry about it later
        if(base64Data.length == 0) { return new byte[0]; }

        int      numberQuadruple    = base64Data.length/FOURBYTE;
        byte     decodedData[];
        byte     b1, b2, b3, b4, marker0, marker1;

        // Throw away anything not in base64Data

        int encodedIndex = 0;
        int dataIndex;
        {
            // this sizes the output array properly - rlw
            int lastData = base64Data.length;
            // ignore the '=' padding
            while (base64Data[lastData-1] == PAD)
            {
                if (--lastData == 0)
                {
                    return new byte[0];
                }
            }
            decodedData = new byte[ lastData - numberQuadruple ];
        }

        for (int i = 0; i < numberQuadruple; i++)
        {
            dataIndex = i * 4;
            marker0   = base64Data[dataIndex + 2];
            marker1   = base64Data[dataIndex + 3];

            b1 = base64Alphabet[base64Data[dataIndex]];
            b2 = base64Alphabet[base64Data[dataIndex +1]];

            if (marker0 != PAD && marker1 != PAD)
            {
                //No PAD e.g 3cQl
                b3 = base64Alphabet[ marker0 ];
                b4 = base64Alphabet[ marker1 ];

                decodedData[encodedIndex]   = (byte)(  b1 <<2 | b2>>4 ) ;
                decodedData[encodedIndex + 1] =
                        (byte)(((b2 & 0xf)<<4 ) |( (b3>>2) & 0xf) );
                decodedData[encodedIndex + 2] = (byte)( b3<<6 | b4 );
            }
            else if (marker0 == PAD)
            {
                //Two PAD e.g. 3c[Pad][Pad]
                decodedData[encodedIndex]   = (byte)(  b1 <<2 | b2>>4 ) ;
            }
            else if (marker1 == PAD)
            {
                //One PAD e.g. 3cQ[Pad]
                b3 = base64Alphabet[ marker0 ];

                decodedData[encodedIndex]   = (byte)(  b1 <<2 | b2>>4 );
                decodedData[encodedIndex + 1] =
                        (byte)(((b2 & 0xf)<<4 ) |( (b3>>2) & 0xf) );
            }
            encodedIndex += 3;
        }
        return decodedData;
    }

    /**
     * Encodes hex octets of a UTF-8 encoded String into Base64
     *
     * @param data the String to encode.
     * @return Encoded Base64 array
     */
    public static byte[] encode(String data) {
        return encode(data.getBytes());
    }

    /**
     * Decodes a string containing Base64 data
     *
     * @param data the String to decode.
     * @return Decoded Base64 array
     */
    public static byte[] decode(String data) {
        return decode(data.getBytes());
    }

    static final int base64[] = {
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 62, 64, 64, 64, 63,
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 64, 64, 64, 64, 64, 64,
            64, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 64, 64, 64, 64, 64,
            64, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64
    };

    public static String base64Decode(String orig) {
        char chars[] = orig.toCharArray();
        StringBuilder sb = new StringBuilder();

        int shift = 0;   // # of excess bits stored in accum
        int acc = 0;

        for (int i = 0; i < chars.length; i++) {
            int v = base64[chars[i] & 0xFF];

            if (v >= 64) {
                // Removed logging at finest level
            } else {
                acc = (acc << 6) | v;
                shift += 6;
                if (shift >= 8) {
                    shift -= 8;
                    sb.append((char) ((acc >> shift) & 0xff));
                }
            }
        }
        return sb.toString();
    }
}
