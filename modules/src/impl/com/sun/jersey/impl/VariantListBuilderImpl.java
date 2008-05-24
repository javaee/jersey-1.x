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

package com.sun.jersey.impl;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class VariantListBuilderImpl extends Variant.VariantListBuilder {

    private List<Variant> variants;
    
    private final List<MediaType> mediaTypes = new ArrayList<MediaType>();
    
    private final List<String> languages = new ArrayList<String>();
   
    private final List<String> charsets = new ArrayList<String>();
    
    private final List<String> encodings = new ArrayList<String>();
    
    @Override
    public List<Variant> build() {
        if (variants == null)
            variants = new ArrayList<Variant>();
        
        return variants;
    }

    @Override
    public VariantListBuilder add() {
        if (variants == null)
            variants = new ArrayList<Variant>();

        addMediaTypes();
        
        charsets.clear();
        languages.clear();
        encodings.clear();
        mediaTypes.clear();

        return this;
    }

    private void addMediaTypes() {
        if (mediaTypes.isEmpty()) addLanguages(null);
        else for (MediaType mediaType : mediaTypes) addLanguages(mediaType);        
    }
    
    private void addLanguages(MediaType mediaType) {
        if (languages.isEmpty()) addEncodings(mediaType, null);
        else for (String language : languages) addEncodings(mediaType, language);        
    }
    
    private void addEncodings(MediaType mediaType, String language) {
        if (encodings.isEmpty()) addVariant(mediaType, language, null);
        else for (String encoding : encodings) addVariant(mediaType, language, encoding);        
    }

    private void addVariant(MediaType mediaType, String language, String encoding) {
        variants.add(new Variant(mediaType, language, encoding));
    }
    
    @Override
    public VariantListBuilder languages(String... languages) {
        for (String language : languages) this.languages.add(language);
        return this;
    }

    @Override
    public VariantListBuilder encodings(String... encodings) {
        for (String encoding : encodings) this.encodings.add(encoding);
        return this;
    }

    @Override
    public VariantListBuilder mediaTypes(MediaType... mediaTypes) {
        for (MediaType mediaType : mediaTypes) this.mediaTypes.add(mediaType);
        return this;
    }
}
