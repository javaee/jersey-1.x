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

package com.sun.jersey.server.impl;

import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.header.AcceptableLanguageTag;
import com.sun.jersey.core.header.AcceptableMediaType;
import com.sun.jersey.core.header.AcceptableToken;
import com.sun.jersey.core.header.QualityFactor;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.server.impl.model.HttpHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class VariantSelector {
    
    private VariantSelector() { }

    /**
     * Interface to get a dimension value from a variant and check if an
     * acceptable dimension value is compatible with a dimension value.
     * 
     * @param T the acceptable dimension value type
     * @param U the dimension value type
     */
    private static interface DimensionChecker<T, U> {
        /**
         * Get the dimension value from the variant
         * 
         * @param v the variant
         * @return the dimension value
         */
        U getDimension(VariantHolder v);
        
        /**
         * Get the quality source of the dimension
         * 
         * @return
         */
        int getQualitySource(VariantHolder v, U u);

        /**
         * Ascertain if the acceptable dimension value is compatible with
         * the dimension value
         * 
         * @param t the acceptable dimension value
         * @param u the dimension value
         * @return true if the acceptable dimension value is compatible with
         *         the dimension value
         */
        boolean isCompatible(T t, U u);
    }
    
    private static final DimensionChecker<AcceptableMediaType, MediaType> MEDIA_TYPE_DC = 
            new DimensionChecker<AcceptableMediaType, MediaType>() {
        public MediaType getDimension(VariantHolder v) {
            return v.v.getMediaType();
        }

        public boolean isCompatible(AcceptableMediaType t, MediaType u) {
            return t.isCompatible(u);
        }

        public int getQualitySource(VariantHolder v, MediaType u) {
            return v.mediaTypeQs;
        }
    };
    
    private static final DimensionChecker<AcceptableLanguageTag, Locale> LANGUAGE_TAG_DC = 
            new DimensionChecker<AcceptableLanguageTag, Locale>() {
        public Locale getDimension(VariantHolder v) {
            return v.v.getLanguage();
        }

        public boolean isCompatible(AcceptableLanguageTag t, Locale u) {
            return t.isCompatible(u);
        }

        public int getQualitySource(VariantHolder qsv, Locale u) {
            return QualityFactor.MINUMUM_QUALITY;
        }
    };
    
    private static final DimensionChecker<AcceptableToken, String> CHARSET_DC = 
            new DimensionChecker<AcceptableToken, String>() {
        public String getDimension(VariantHolder v) {
            MediaType m = v.v.getMediaType();
            return (m != null) ? m.getParameters().get("charset") : null;
        }

        public boolean isCompatible(AcceptableToken t, String u) {
            return t.isCompatible(u);
        }        

        public int getQualitySource(VariantHolder qsv, String u) {
            return QualityFactor.MINUMUM_QUALITY;
        }
    };
    
    private static final DimensionChecker<AcceptableToken, String> ENCODING_DC = 
            new DimensionChecker<AcceptableToken, String>() {
        public String getDimension(VariantHolder v) {
            return v.v.getEncoding();
        }

        public boolean isCompatible(AcceptableToken t, String u) {
            return t.isCompatible(u);
        }        

        public int getQualitySource(VariantHolder qsv, String u) {
            return QualityFactor.MINUMUM_QUALITY;
        }
    };
    
    /**
     * Select variants for a given dimension. 
     * 
     * @param the collection of variants.
     * 
     * @param as the list of acceptable dimension values, ordered by the quality
     *        parameter, with the highest quality dimension value occurring
     *        first.
     * @param dc the dimension checker
     */
    private static <T extends QualityFactor, U> LinkedList<VariantHolder> selectVariants(
            Collection<VariantHolder> vs,
            List<T> as,
            DimensionChecker<T, U> dc) {
        int cq = QualityFactor.MINUMUM_QUALITY;
        int cqs = QualityFactor.MINUMUM_QUALITY;

        final LinkedList<VariantHolder> selected = new LinkedList<VariantHolder>();
        final LinkedList<VariantHolder> noDimension = new LinkedList<VariantHolder>();

        // Iterate over the acceptable entries
        // This assumes the entries are ordered by the quality
        for (final T a : as) {
            final int q = a.getQuality();

            for (VariantHolder v : vs) {
                // Get the dimension  value of the variant to check
                final U d = dc.getDimension(v);

                if (d != null) {
                    // Check if the acceptable entry is compatable with
                    // the dimension value
                    if (dc.isCompatible(a, d)) {
                        final int qs = dc.getQualitySource(v, d);

                        if (qs > cqs) {
                            cqs = qs;
                            cq = q;
                            // Clear an entries that were added for qs < cqs
                            selected.clear();
                            selected.add(v);
                        } else if (qs == cqs) {
                            if (q > cq) {
                                cq = q;
                                selected.addFirst(v);
                            } else if (q == cq) {
                                selected.add(v);
                            }
                        }
                    }
                } else {
                    noDimension.add(v);
                }
            }
        }

        for (VariantHolder v : noDimension) {
            selected.add(v);
        }
        return selected;
    }

    private static class VariantHolder {
        private final Variant v;
        
        private final int mediaTypeQs;

        public VariantHolder(Variant v) {
            this(v, QualitySourceMediaType.DEFAULT_QUALITY_SOURCE_FACTOR);
        }

        public VariantHolder(Variant v, int mediaTypeQs) {
            this.v = v;
            this.mediaTypeQs = mediaTypeQs;
        }
    }

    private static List<VariantHolder> getVariantHolderList(final List<Variant> variants) {
        final ArrayList<VariantHolder> l = new ArrayList<VariantHolder>(variants.size());
        for (Variant v : variants) {
            final MediaType mt = v.getMediaType();
            if (mt != null) {
                if (mt instanceof QualitySourceMediaType || mt.getParameters().
                        containsKey(QualitySourceMediaType.QUALITY_SOURCE_FACTOR)) {
                    int qs = QualitySourceMediaType.getQualitySource(mt);
                    l.add(new VariantHolder(v, qs));
                } else {
                    l.add(new VariantHolder(v));
                }
            } else {
                l.add(new VariantHolder(v));
            }
        }

        return l;
    }

    public static Variant selectVariant(HttpRequestContext r, List<Variant> variants) {
        List<VariantHolder> vhs = getVariantHolderList(variants);
        
        vhs = selectVariants(vhs, HttpHelper.getAccept(r), MEDIA_TYPE_DC);
        vhs = selectVariants(vhs, HttpHelper.getAcceptLanguage(r), LANGUAGE_TAG_DC);
        vhs = selectVariants(vhs, HttpHelper.getAcceptCharset(r), CHARSET_DC);
        vhs = selectVariants(vhs, HttpHelper.getAcceptEncoding(r), ENCODING_DC);

        return (vhs.isEmpty()) ? null : vhs.iterator().next().v;
    }
}
