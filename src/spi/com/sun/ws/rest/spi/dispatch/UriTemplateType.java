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

import com.sun.ws.rest.api.core.UriComponent;
import com.sun.ws.rest.spi.SpiMessages;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A URI template.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriTemplateType {
    /**
     * Order the templates according to the template with the highest number
     * of template variables first. 
     * <p>
     * If two templates have the same number of template variables then order 
     * according to the string comparison of the template regular expressions.
     * A template with a more explicit template regular expression (more
     * characters) occurs before a template with a less explicit regular
     * expression.
     */
    static public final Comparator<UriTemplateType> COMPARATOR = new Comparator<UriTemplateType>() {
        public int compare(UriTemplateType o1, UriTemplateType o2) {
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
            
            
// This code should be uncommented when the specification changes
// the order of the primary and secondary keys
//
//            // Compare the template regular expressions, if the number of
//            // templates are equal.
//            // Note that it is important that o2 is compared against o1
//            // so that a regular expression with more characters (more explicit) 
//            // is less than a regular expression with less characters.
//            int i = o2.getTemplateRegex().compareTo(o1.getTemplateRegex());
//            if (i != 0) return i;
//            
//            // Compare the number of templates
//            return o2.getNumberOfTemplateVariables() - o1.getNumberOfTemplateVariables();
            
            // Compare the number of templates
            int i = o2.getNumberOfTemplateVariables() - o1.getNumberOfTemplateVariables();
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
    
    public static final UriTemplateType NULL = new UriTemplateType();
            
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
     * The template variables in the URI template.
     */
    private final List<String> templateVariables;
    
    /**
     * The regular expression for matching URIs and obtaining template values.
     */
    private final String templateRegex;
    
    /**
     * The compiled regular expression of {@link #templateRegex}
     */
    private final Pattern templateRegexPattern;

    // Constructor for NULL template
    private UriTemplateType() {
        this.template = "";
        this.rightHandPattern = null;
        this.endsWithSlash = false;
        this.templateVariables = Collections.emptyList();
        this.templateRegex = "";
        this.templateRegexPattern = null;
    }
    
    /**
     * Construct a new URI template.
     * <p>
     * The template will be parsed to extract template variables.
     * <p>
     * A specific regular expression will be generated from the template
     * to match URIs according to the template and map template variables to
     * template values.
     * <p>
     * @param template the template.
     * @throws {@link java.util.regex.PatternSyntaxException} if the specific
     *         regular expression could not be generated
     * @throws {@link IllegalArgumentException} if the template is null or
     *         an empty string.
     */
    public UriTemplateType(String template) {
        this(template, null);
    }
    
    /**
     * Construct a new URI template.
     * <p>
     * The template will be parsed to extract template variables.
     * <p>
     * A specific regular expressions will be generated from the template
     * to match URIs according to the template and map template variables to
     * template values. 
     * <p>
     * @param template the template.
     * @param limited if true the right hand expression "(/.*)?" is appended 
     *        to the regular expression generated from the URI template,
     *        otherwise the expression "(/)?" is appended.
     * @throws {@link java.util.regex.PatternSyntaxException} if the specific
     *         regular expression could not be generated
     * @throws {@link IllegalArgumentException} if the template is null or
     *         an empty string.
     */
    public UriTemplateType(String template, boolean limited) {
        this(template, (limited) ? UriTemplateType.RIGHT_HANDED_REGEX : 
            UriTemplateType.RIGHT_SLASHED_REGEX);        
    }
    
    /**
     * Construct a new URI template.
     * <p>
     * The template will be parsed to extract template variables.
     * <p>
     * A specific regular expressions will be generated from the template
     * to match URIs according to the template and map template variables to
     * template values. 
     * <p>
     * @param template the template.
     * @param rightHandPattern the right hand pattern to be appended
     *        to the regular expression generated from the URI template.
     * @throws {@link java.util.regex.PatternSyntaxException} if the specific
     *         regular expression could not be generated
     * @throws {@link IllegalArgumentException} if the template is null or
     *         an empty string.
     */
    public UriTemplateType(String template, String rightHandPattern) {
        if (template == null)
            throw new IllegalArgumentException(SpiMessages.URITEMPLATE_CANNOT_BE_NULL());
        
        if (template.length() == 0 && (rightHandPattern == null || rightHandPattern.length() == 0))
            throw new IllegalArgumentException(SpiMessages.TEMPLATE_NAME_TO_VALUE_NOT_NULL());

        this.template = template;
        this.rightHandPattern = rightHandPattern;

        // TODO correctly validate template
        
        StringBuilder b = new StringBuilder();
        List<String> names = new ArrayList<String>();
        
        // Find all template variables
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

        templateVariables = Collections.unmodifiableList(names);
        
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
    public final String getTemplate() {
        return template;
    }
    
    /**
     * Check if the URI template is left handed.
     * @return true if the URI template is left handed, otherwise false.
     */
    public final boolean isLeftHanded() {
        return rightHandPattern != null;
    }
    
    public final boolean endsWithSlash() {
        return endsWithSlash;
    }
    
    /**
     * Get the list of template variables for the template.
     * @return the list of template variables.
     */
    public final List<String> getTemplateVariables() {
        return templateVariables;
    }
    
    /**
     * Ascertain if a template variable is a member of this
     * template.
     * @param name name The template variable.
     * @return true if the template variable is a member of the template, otherwise
     * false.
     */
    public final boolean isTemplateVariablePresent(String name) {
        for (String s : templateVariables) {
            if (s.equals(name))
                return true;
        }
        
        return false;
    }
    
    /**
     * Get the number of template variables.
     * @return the number of template variables.
     */
    public final int getNumberOfTemplateVariables() {
        return templateVariables.size();
    }
    
    /**
     * Get the regular expression used for matching URIs to the template.
     * @return the regular expression for matching URIs.
     */
    public final String getTemplateRegex() {
        return templateRegex;
    }

    /**
     * Match a URI against the template.
     * <p>
     * If the URI matches against the pattern then the template variable to value 
     * map will be filled with template variables as keys and template values as 
     * values.
     * <p>
     * 
     * @param uri the uri to match against the template.
     * @param templateVariableToValue the map where to put template variables (as keys)
     *        and template values (as values). The map is cleared before any
     *        entries are put. The matching group for a right hand expression (if any)
     *        is placed in the map using the NULL key.
     * @return true if the URI matches the template, otherwise false.
     * @throws {@link IllegalArgumentException} if the uri or 
     *         templateVariableToValue is null.
     */
    public final boolean match(CharSequence uri, Map<String, String> templateVariableToValue) {
        if (templateVariableToValue == null) 
            throw new IllegalArgumentException(SpiMessages.TEMPLATE_NAME_TO_VALUE_NOT_NULL());

        templateVariableToValue.clear();
                
        if (uri == null || uri.length() == 0)
            return (templateRegexPattern == null) ? true : false;
        
        if (templateRegexPattern == null)
            return false;
                
        // Match the URI to the URI template regular expression
        Matcher m = templateRegexPattern.matcher(uri);
        if (!m.matches())
            return false;

        // Assign the matched template values to template variables
        int i = 1;
        for (String name : templateVariables) {
            String previousValue = templateVariableToValue.get(name);
            String currentValue = m.group(i++);
            
            // URI templates can have the same template variable
            // occuring more than once, check that the template
            // values are same.
            if (previousValue != null && !previousValue.equals(currentValue))
                return false;
            
            templateVariableToValue.put(name, currentValue);
        }
        
        // Assign the right hand side value to the null key
        if (rightHandPattern != null) {
            templateVariableToValue.put(null, m.group(i));
        }
        
        return true;
    }
    
    /**
     * Match a URI against the template.
     * <p>
     * If the URI matches against the pattern then the template variable to value 
     * map will be filled with template variables as keys and template values as 
     * values.
     * <p>
     * 
     * @param uri the uri to match against the template.
     * @param rightHandGroup the StringBuilder to place the right hand group of
     *        the matching right hand expression. The rightHandPath and the uri
     *        may refer to the same StringBuilder instance.
     * @param templateVariableToValue the map where to put template variables (as keys)
     *        and template values (as values). The map is cleared before any
     *        entries are put.
     * @return true if the URI matches the template, otherwise false.
     * @throws {@link IllegalArgumentException} if the uri or 
     *         templateVariableToValue is null.
     */
    public final boolean match(CharSequence uri, StringBuilder rightHandGroup, 
            Map<String, String> templateVariableToValue) {
        if (templateVariableToValue == null) 
            throw new IllegalArgumentException(SpiMessages.TEMPLATE_NAME_TO_VALUE_NOT_NULL());

        templateVariableToValue.clear();
                
        if (uri == null || uri.length() == 0)
            return (templateRegexPattern == null) ? true : false;
        
        if (templateRegexPattern == null)
            return false;
                
        // Match the URI to the URI template regular expression
        Matcher m = templateRegexPattern.matcher(uri);
        if (!m.matches())
            return false;

        // Assign the matched template values to template variables
        int i = 1;
        for (String name : templateVariables) {
            String previousValue = templateVariableToValue.get(name);
            String currentValue = m.group(i++);
            
            // URI templates can have the same template variable
            // occuring more than once, check that the template
            // values are same.
            if (previousValue != null && !previousValue.equals(currentValue))
                return false;
            
            templateVariableToValue.put(name, currentValue);
        }

        // Return the matching right hand group
        if (rightHandPattern != null) {
            // Check the same StringBuilder is used for the URI and the right
            // hand group
            if (uri == rightHandGroup) {
                i = m.start(i);
                // Remove the left hand path
                if (i >= 0)
                    rightHandGroup.delete(0, i);
                else
                    rightHandGroup.setLength(0);
            } else {
                // Reset the right hand group
                rightHandGroup.setLength(0);
                final String rightHandGroupString = m.group(i);
                if (rightHandGroupString != null)
                    rightHandGroup.append(rightHandGroupString);        
            }
        }
        
        return true;
    }
    
    /**
     * Create a URI by substituting any template variables
     * for corresponding template values.
     * <p>
     * A URI template varibale without a value will be substituted by the 
     * empty string.
     *
     * @param values the map of template variables to template values.
     * @return the URI.
     */
    public final String createURI(Map<String, String> values) {
        StringBuilder b = new StringBuilder();
        // Find all template variables
        Matcher m = TEMPLATE_NAMES_PATTERN.matcher(template);
        int i = 0;
        while(m.find()) {
            b.append(template, i, m.start());
            String tValue = values.get(m.group(1));
            if (tValue != null) b.append(tValue);
            i = m.end();
        }
        b.append(template, i, template.length());
        return b.toString();
    }
    
    /**
     * Create a URI by substituting any template variables
     * for corresponding template values.
     * <p>
     * A URI template varibale without a value will be substituted by the 
     * empty string.
     *
     * @param values the array of template values. The values will be 
     *        substituted in order of occurence of unique template variables.
     * @return the URI.
     */
    public final String createURI(String... values) {
        return createURI(values, 0, values.length);
    }
    
    /**
     * Create a URI by substituting any template variables
     * for corresponding template values.
     * <p>
     * A URI template varibale without a value will be substituted by the 
     * empty string.
     *
     * @param values the array of template values. The values will be 
     *        substituted in order of occurence of unique template variables.
     * @param offset the offset into the array
     * @param length the length of the array
     * @return the URI.
     */
    public final String createURI(String[] values, int offset, int length) {
        Map<String, String> mapValues = new HashMap<String, String>();
        StringBuilder b = new StringBuilder();
        // Find all template variables
        Matcher m = TEMPLATE_NAMES_PATTERN.matcher(template);
        int v = offset;
        length += offset;
        int i = 0;
        while(m.find()) {
            b.append(template, i, m.start());
            String tVariable = m.group(1);
            // Check if a template variable has already occurred
            // If so use the value to ensure that two or more declarations of 
            // a template variable have the same value
            String tValue = mapValues.get(tVariable);
            if (tValue != null) {
                b.append(tValue);
            } else {
                if (v < length) {
                    tValue = values[v++];
                    if (tValue != null) {
                        mapValues.put(tVariable, tValue);
                        b.append(tValue);
                    }
                }
            }
            i = m.end();
        }
        b.append(template, i, template.length());
        return b.toString();
    }
    
    public final String toString() {
        return templateRegex;
    }
    
    public final int hashCode() {
        return template.hashCode();
    }

    public final boolean equals(Object o) {
        if (o instanceof UriTemplateType) {
            UriTemplateType that = (UriTemplateType)o;
            if (template == that.template)
                return true;
            return template.equals(that.template);
        } else {
            return false;
        }
    }
    
    /**
     * Construct a URI from the component parts each of which may contain 
     * template variables.
     *
     * @param scheme the URI scheme component
     * @param userInfo the URI user info component
     * @param host the URI host component
     * @param port the URI port component
     * @param path the URI path component
     * @param query the URI query componnet
     * @param fragment the URI fragment component
     * @param values the template variable to value map
     * @param encode if true encode a template value according to the correspond
     *        component type of the associated template variable, otherwise
     *        validate the template value
     * @return a URI
     */
    public static String createURI(final String scheme, 
            final String userInfo, final String host, final String port, 
            final String path, final String query, final String fragment,
            final Map<String, String> values, final boolean encode) {
        
        StringBuilder sb = new StringBuilder();
        
        if (scheme != null)
            createURIComponent(UriComponent.Type.SCHEME, scheme, values, false, sb).
                    append(':');
        
        if (userInfo != null || host != null || port != null) {
            sb.append("//");
            
            if (userInfo != null && userInfo.length() > 0) 
                createURIComponent(UriComponent.Type.USER_INFO, userInfo, values, encode, sb).
                    append('@');
            
            if (host != null) {
                // TODO check IPv6 address
                createURIComponent(UriComponent.Type.HOST, host, values, encode, sb);
            }

            if (port != null && port.length() > 0) {
                sb.append(':');
                createURIComponent(UriComponent.Type.PORT, port, values, false, sb);                
            }
        }

        if (path != null)
            createURIComponent(UriComponent.Type.PATH, path, values, encode, sb);                
        
        if (query != null && query.length() > 0) {
            sb.append('?');
            createURIComponent(UriComponent.Type.QUERY, query, values, encode, sb);
        }
         
        if (fragment != null && fragment.length() > 0) {
            sb.append('#');
            createURIComponent(UriComponent.Type.FRAGMENT, path, values, encode, sb);                
        }
        return sb.toString();
    }
    
    private static StringBuilder createURIComponent(final UriComponent.Type t, final String template, 
            final Map<String, String> values, 
            final boolean encode, 
            final StringBuilder b) {
        if (template.indexOf('{') == -1) {
            b.append(template);
            return b;
        }
        
        // Find all template variables
        final Matcher m = TEMPLATE_NAMES_PATTERN.matcher(template);
        int i = 0;
        while(m.find()) {
            b.append(template, i, m.start());
            String tValue = values.get(m.group(1));
            if (tValue != null) {
                if (encode)
                    tValue = UriComponent.encode(tValue, t);
                else
                    UriComponent.validate(tValue, t);
                b.append(tValue);
            }
            i = m.end();
        }
        b.append(template, i, template.length());
        return b;
    }
    
    /**
     * Construct a URI from the component parts each of which may contain 
     * template variables.
     *
     * @param scheme the URI scheme component
     * @param userInfo the URI user info component
     * @param host the URI host component
     * @param port the URI port component
     * @param path the URI path component
     * @param query the URI query componnet
     * @param fragment the URI fragment component
     * @param values the array of template values
     * @param encode if true encode a template value according to the correspond
     *        component type of the associated template variable, otherwise
     *        validate the template value
     * @return a URI
     */
    public static String createURI(final String scheme, 
            final String userInfo, final String host, final String port, 
            final String path, final String query, final String fragment,
            final String[] values, final boolean encode) {
        
        final Map<String, String> mapValues = new HashMap<String, String>();
        final StringBuilder sb = new StringBuilder();
        int offset = 0;
        
        if (scheme != null) {
            offset = createURIComponent(UriComponent.Type.SCHEME, scheme, values, offset, false, mapValues, sb);
            sb.append(':');
        }
        
        if (userInfo != null || host != null || port != null) {
            sb.append("//");
            
            if (userInfo != null && userInfo.length() > 0) {
                offset = createURIComponent(UriComponent.Type.USER_INFO, userInfo, values, offset, encode, mapValues, sb);
                sb.append('@');
            }
            
            if (host != null) {
                // TODO check IPv6 address
                offset = createURIComponent(UriComponent.Type.HOST, host, values, offset, encode, mapValues, sb);
            }

            if (port != null && port.length() > 0) {
                sb.append(':');
                offset = createURIComponent(UriComponent.Type.PORT, port, values, offset, false, mapValues, sb);                
            }
        }

        if (path != null)
            offset = createURIComponent(UriComponent.Type.PATH, path, values, offset, encode, mapValues, sb);                
        
        if (query != null && query.length() > 0) {
            sb.append('?');
            offset = createURIComponent(UriComponent.Type.QUERY, query, values, offset, encode, mapValues, sb);
        }
         
        if (fragment != null && fragment.length() > 0) {
            sb.append('#');
            offset = createURIComponent(UriComponent.Type.FRAGMENT, path, values, offset, encode, mapValues, sb);                
        }
        return sb.toString();
    }
    
    private static int createURIComponent(final UriComponent.Type t, final String template,
            final String[] values, final int offset,
            final boolean encode, 
            final Map<String, String> mapValues,
            final StringBuilder b) {
        // Find all template variables
        final Matcher m = TEMPLATE_NAMES_PATTERN.matcher(template);
        int v = offset;
        int i = 0;
        while(m.find()) {
            b.append(template, i, m.start());
            final String tVariable = m.group(1);
            // Check if a template variable has already occurred
            // If so use the value to ensure that two or more declarations of 
            // a template variable have the same value
            String tValue = mapValues.get(tVariable);
            if (tValue != null) {
                b.append(tValue);
            } else {
                if (v < values.length) {
                    tValue = values[v++];
                    if (tValue != null) {
                        mapValues.put(tVariable, tValue);
                        if (encode)
                            tValue = UriComponent.encode(tValue, t);
                        else
                            UriComponent.validate(tValue, t);
                        b.append(tValue);
                    }
                }
            }
            i = m.end();
        }
        b.append(template, i, template.length());
        return v;
    }
}
