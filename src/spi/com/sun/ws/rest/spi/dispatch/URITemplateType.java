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

package com.sun.ws.rest.spi.dispatch;

import com.sun.ws.rest.spi.SpiMessages;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * URI template implementation.
 * <p>
 * @author Paul.Sandoz@Sun.Com
 */
public final class URITemplateType {
    /**
     * Order the templates according to the template with the highest number
     * of template values first. 
     * <p>
     * If two templates have the same number of template values then order 
     * according to the string comparison of the template regular expressions.
     * A template with a more explicit template regular expression (more
     * characters) occurs before a template with a less explicit regular
     * expression.
     */
    static public final Comparator<URITemplateType> COMPARATOR = new Comparator<URITemplateType>() {
        public int compare(URITemplateType o1, URITemplateType o2) {
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null)
                return 1;
            if (o2 == null)
                return -1;
            
            if (o1 == NULL && o2 == NULL)
                return 0;
            if (o1 == NULL)
                return 1;
            if (o2 == NULL)
                return -1;
            
            
            // Compare the number of templates
            int i = o2.getNumberOfTemplateNames() - o1.getNumberOfTemplateNames();
            if (i != 0) return i;

            // Compare the template regular expressions, if the number of
            // templates are equal.
            // Note that it is important that o2 is compared against o1
            // so that a regular expression with more characters (more explicit) 
            // is less than a regular expression with less characters.
            return o2.getTemplateRegex().compareTo(o1.getTemplateRegex());
        }
    };
    
    /**
     * The regular expression for matching URI templates and names.
     */
    private static final Pattern TEMPLATE_NAMES_PATTERN = Pattern.compile("\\{([\\w-\\._~]+?)\\}");
    
    /**
     * This uses a reluctant (non-greedy qualifier) to ensure that
     * expresions to the right of an expression will be matched.
     */
    private static final String TEMPLATE_VALUE_REGEX = "(.*?)";
    
    /**
     * The regular expression that represents the right hand side of
     * a URI path.
     */
    public static final String RIGHT_HANDED_REGEX = "(/.*)?";
        
    /**
     * The regular expression that represents the right hand side of
     * a URI path that is a '/' or null.
     */
    public static final String RIGHT_SLASHED_REGEX = "(/)?";
    
    public static final URITemplateType NULL = new URITemplateType();
            
    /**
     * The URI template.
     */
    private final String template;
    
    private final String rightHandPattern;
    
    /**
     * True if the URI template ends in a '/' character.
     */
    private final boolean endsWithSlash;
    
    /**
     * The names of templates in the URI template.
     */
    private final List<String> templateNames;
    
    /**
     * The regular expression for matching URIs and obtaining template values.
     */
    private final String templateRegex;
    
    /**
     * The compiled regular expression of {@link #templateRegex}
     */
    private final Pattern templateRegexPattern;

    // Constructor for NULL template
    private URITemplateType() {
        this.template = "";
        this.rightHandPattern = null;
        this.endsWithSlash = false;
        this.templateNames = Collections.emptyList();
        this.templateRegex = "";
        this.templateRegexPattern = null;
    }
    
    /**
     * Construct a new URI template.
     * <p>
     * The template will be parsed to extract template names.
     * <p>
     * A specific regular expressions will be generated from the template
     * to match URIs according to the template and map template names to
     * template values. 
     * <p>
     * @param template the template.
     * @throws {@link java.util.regex.PatternSyntaxException} if the specific
     *         regular expression could not be generated
     * @throws {@link IllegalArgumentException} if the template is null or
     *         an empty string.
     */
    public URITemplateType(String template) {
        this(template, null);
    }
    
    /**
     * Construct a new URI template.
     * <p>
     * The template will be parsed to extract template names.
     * <p>
     * A specific regular expressions will be generated from the template
     * to match URIs according to the template and map template names to
     * template values. 
     * <p>
     * @param template the template.
     * @param rightHandPattern the right hand pattern if this URI template
     *        represents the left hand pattern.
     * @throws {@link java.util.regex.PatternSyntaxException} if the specific
     *         regular expression could not be generated
     * @throws {@link IllegalArgumentException} if the template is null or
     *         an empty string.
     */
    public URITemplateType(String template, String rightHandPattern) {
        if (template == null)
            throw new IllegalArgumentException(SpiMessages.URITEMPLATE_CANNOT_BE_NULL());
        
        if (template.length() == 0 && (rightHandPattern == null || rightHandPattern.length() == 0))
            throw new IllegalArgumentException(SpiMessages.TEMPLATE_NAME_TO_VALUE_NOT_NULL());

        try {
            // TODO should only contain valuid URI characters plus '{' and '}'?
            // Decode the template to process escaped characters
            template = URLDecoder.decode(template, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Template cannot be decoded", e);
        }
        
        this.template = template;
        this.rightHandPattern = rightHandPattern;
                
        StringBuilder b = new StringBuilder();
        List<String> names = new ArrayList<String>();
        
        // Find all template names
        // Create regular expression for template matching
        Matcher m = TEMPLATE_NAMES_PATTERN.matcher(template);
        int i = 0;
        while(m.find()) {
            copyURITemplateCharacters(template, i, m.start(), b);
            b.append(TEMPLATE_VALUE_REGEX);
            names.add(m.group(1));
            i = m.end();
        }
        copyURITemplateCharacters(template, i, template.length(), b);

        templateNames = Collections.unmodifiableList(names);
        
        int endPos = b.length() - 1;
        this.endsWithSlash = (endPos >= 0) ? b.charAt(endPos) == '/' : false;
        if (rightHandPattern != null) {
            // Remove '/' character at the end of template
            // If the template is '/' then it will be replaced
            // with '(/.*)?'
            if (endsWithSlash)
                b.deleteCharAt(endPos);
            b.append(rightHandPattern);
        }
        
        templateRegex = b.toString();
        templateRegexPattern = Pattern.compile(templateRegex);
    }
    
    /**
     * Copy characters from the URI template to a string builder.
     * Characters that correspond to regular expression
     * characters will be escaped.
     * <p>
     * TODO need to escape all regex characters present
     *
     * @param template The URI template.
     * @param start The start index of characters in template to copy
     * @param end The end index of characters in template to copy
     * @param b The string builder to copy the characters.
     */
    private void copyURITemplateCharacters(String template, int start, int end, StringBuilder b) {
        for (int i = start; i < end; i++) {
            char c = template.charAt(i);
            // TODO need to escape all regex characters present
            if (c == '?') b.append("\\?");
            else b.append(c);
        }
    }

    /**
     * Get the URI template as a String.
     * @return the URI template.
     */
    public String getTemplate() {
        return template;
    }
    
    /**
     * Check if the URI template is left handed.
     * @return true if the URI template is left handed, otherwise false.
     */
    public boolean isLeftHanded() {
        return rightHandPattern != null;
    }
    
    public boolean endsWithSlash() {
        return endsWithSlash;
    }
    
    /**
     * Get the list of template names for the template.
     * @return the list of template names.
     */
    public List<String> getTemplateNames() {
        return templateNames;
    }
    
    /**
     * Ascertain if a template name is a member of this
     * template.
     * @param name name The template name.
     * @return true if the template name is a member of the template, otherwise
     * false.
     */
    public boolean isTemplateNamePresent(String name) {
        for (String s : templateNames) {
            if (s.equals(name))
                return true;
        }
        
        return false;
    }
    
    /**
     * Get the number of template names.
     * @return the number of tempalte names.
     */
    public int getNumberOfTemplateNames() {
        return templateNames.size();
    }
    
    /**
     * Get the regular expression used for matching URIs to the template.
     * @return the regular expression for matching URIs.
     */
    public String getTemplateRegex() {
        return templateRegex;
    }

    /**
     * Match a URI against the template.
     * <p>
     * If the URI matches against the pattern then the template name to value 
     * map will be filled with template names as keys and template values as 
     * values.
     * <p>
     * @param uri the uri to match against the template.
     * @param templateNameToValue the map where to put template names (as keys)
     *        and template values (as values). The map is cleared before any
     *        entries are added.
     * @return true if the URI matches the template, otherwise false.
     * @throws {@link IllegalArgumentException} if the uri or 
     *         templateNameToValue is null.
     */
    public boolean match(String uri, Map<String, String> templateNameToValue) {
        if (templateNameToValue == null) 
            throw new IllegalArgumentException(SpiMessages.TEMPLATE_NAME_TO_VALUE_NOT_NULL());

        templateNameToValue.clear();
                
        if (uri == null)
            return (templateRegexPattern == null) ? true : false;
        
        if (templateRegexPattern == null)
            return false;
                
        // Match the URI to the URI template regular expression
        Matcher m = templateRegexPattern.matcher(uri);
        if (!m.matches())
            return false;

        // Assign the matched template values to template names
        int i = 1;
        for (String name : templateNames) {
            String previousValue = templateNameToValue.get(name);
            String currentValue = m.group(i++);
            
            // URI templates can have the same template name
            // occuring more than once, check that the template
            // values are same.
            if (previousValue != null && !previousValue.equals(currentValue))
                return false;
            
            templateNameToValue.put(name, currentValue);
        }
        
        // Assign the right hand side value to the null key
        if (rightHandPattern != null) {
            templateNameToValue.put(null, m.group(i));
        }
        
        return true;
    }
    
    public String toString() {
        return templateRegex;
    }
    
    public int hashCode() {
        return template.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof URITemplateType) {
            URITemplateType that = (URITemplateType)o;
            if (template == that.template)
                return true;
            return template.equals(that.template);
        } else {
            return false;
        }
    }
}
