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
package com.sun.jersey.impl.wadl.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jersey.impl.wadl.WadlGenerator;

/**
 * An implementation of {@link WadlGeneratorConfiguration} that can be configured with
 * a configuration string (see {@link WadlGeneratorConfig#WadlGeneratorConfig(Map)}),
 * a list of {@link WadlGeneratorDescription}s or with an already initialized {@link WadlGenerator}.<br/>
 * 
 * This class also provides methods for building a configured instance, see
 * {@link WadlGeneratorConfig#generator(String)} and {@link WadlGeneratorConfig#generator(WadlGenerator)}.
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorConfig implements WadlGeneratorConfiguration {

    private static final Logger LOGGER = Logger.getLogger( WadlGeneratorConfig.class.getName() );

    private static final Pattern PATTERN_CHECK = Pattern.compile( "[\\w.$]+\\[(\\w+=[^,\\]]+(,\\w+=[^,\\]]+)*)?\\](?:\\s*;\\s*(?:[\\w.$]+\\[(\\w+=[^,\\]]+(,\\w+=[^,\\]]+)*)?\\]))*" );
    private static final Pattern PATTERN_FIND_CONFIGURATIONS = Pattern.compile( "(([\\w.$]+)\\[([^\\]]*)\\])" );
    private static final Pattern PATTERN_FIND_PROPERTIES = Pattern.compile( "((\\w+)=([^,]+))" );
    
    /**
     * The property value MUST be an instance String 
     * representing one or more wadl generator configurations that MUST be separated by ';'.<br/>
     * <br/>
     * A wadl generator configuration has the following format:
     * 
     * <pre><code>&lt;classname&gt;[&lt;property&gt;=&lt;value&gt;]</code></pre>
     * 
     * Properties can be of type {@link String}, {@link File} or other types that provide
     * a {@link String} constructor.
     * 
     * If the property is of type {@link File}, then the property value can start with the
     * prefix <em>classpath:</em> to denote, that the File shall be loaded from the classpath like this:
     * <pre><code>new File( generator.getClass().getResource( strippedFilename ).toURI() )</code></pre>
     * Notice that the file is loaded as a resource from the classpath in this case, therefore <em>classpath:test.xml</em>
     * refers to a file in the package of the specified <code>&lt;classname&gt;</code>. The
     * file reference <em>classpath:/test.xml</em> refers to a file that is in the root of the classpath.
     * </li>
     * 
     * You can e.g. specify this:
     * 
     * <pre><code>
     * com.sun.jersey.impl.wadl.generators.WadlGeneratorApplicationDoc[applicationDocsFile=classpath:/application-doc.xml];
     * com.sun.jersey.impl.wadl.generators.WadlGeneratorGrammarsSupport[grammarsFile=classpath:/application-grammars.xml]
     * </code></pre>
     * 
     * @see WadlGeneratorDescription
     * @see WadlGeneratorLoader
     */
    public static final String PROPERTY_WADL_GENERATOR_DESCRIPTIONS
            = "com.sun.jersey.config.property.wadlGeneratorDescription";

    private WadlGenerator _wadlGenerator;
    
    public WadlGeneratorConfig() {
        this( (List<WadlGeneratorDescription>)null );
    }

    /**
     * Creates a new WadlGeneratorConfig from the provided properties map, that must
     * contain a property {@link #PROPERTY_WADL_GENERATOR_DESCRIPTIONS}.
     * @param props the properties map, must not be null.
     */
    public WadlGeneratorConfig( Map<String, Object> props ) {
        this( loadWadlGeneratorDescriptions( props ) );
    }

    /**
     * Creates a new WadlGeneratorConfig from the provided wadlGeneratorDescriptions (can be null).
     * @param wadlGeneratorDescriptions the descriptions for wadl generators, can be null.
     */
    public WadlGeneratorConfig(
            List<WadlGeneratorDescription> wadlGeneratorDescriptions) {
        try {
            _wadlGenerator = WadlGeneratorLoader.loadWadlGeneratorDescriptions( wadlGeneratorDescriptions );
        } catch ( Exception e ) {
            throw new RuntimeException( "Could not load wadl generators from wadlGeneratorDescriptions.", e );
        }
    }

    /**
     * Creates a new WadlGeneratorConfig from the provided wadlGenerator (must already be initialized).
     * @param wadlGenerator the wadl generator to provide, must not be null.
     */
    public WadlGeneratorConfig( WadlGenerator wadlGenerator ) {
        if ( wadlGenerator == null ) {
            throw new IllegalArgumentException( "The wadl generator must not be null." );
        }
        _wadlGenerator = wadlGenerator;
    }

    /**
     * @return the wadlGeneratorDescriptions
     */
    public WadlGenerator getWadlGenerator() {
        return _wadlGenerator;
    }

    protected static List<WadlGeneratorDescription> loadWadlGeneratorDescriptions(
            Map<String, Object> props ) {

        final String value = (String) props.get( PROPERTY_WADL_GENERATOR_DESCRIPTIONS );
        if (value == null) {
            throw new IllegalArgumentException( PROPERTY_WADL_GENERATOR_DESCRIPTIONS + 
                    " property is missing" );
        }
        
        if ( !PATTERN_CHECK.matcher( value ).matches() ) {
            throw new IllegalArgumentException( PROPERTY_WADL_GENERATOR_DESCRIPTIONS + 
             " property has bad format, must conform to " + PATTERN_CHECK.pattern() +
             " (actual value: " + value + ")" );
        }
        
        final Matcher matcher = PATTERN_FIND_CONFIGURATIONS.matcher( value );
        final List<WadlGeneratorDescription> wadlGeneratorDescriptions = new ArrayList<WadlGeneratorDescription>();
        
        while( matcher.find() ) {
            
            /* we expect 3 groups here
             */
            final String className = matcher.group( 2 );
            LOGGER.info( "Have className " + className );
            
            final Properties generatorProps = new Properties();
            final String propertiesString = matcher.group( 3 );
            final Matcher propertiesMatcher = PATTERN_FIND_PROPERTIES.matcher( propertiesString );
            while( propertiesMatcher.find() ) {
                final String propName = propertiesMatcher.group( 2 );
                final String propValue = propertiesMatcher.group( 3 );
                LOGGER.info( "have propName: " + propName + ", propValue: " + propValue );
                generatorProps.put( propName, propValue );
            }
            
            wadlGeneratorDescriptions.add( new WadlGeneratorDescription( className, generatorProps ) );
            
        }
        
        return wadlGeneratorDescriptions;
    }

    /**
     * Start to build an instance of {@link WadlGeneratorConfig}:
     * <pre><code>generator(&lt;class&gt;)
     *      .prop(&lt;name&gt;, &lt;value&gt;)
     *      .prop(&lt;name&gt;, &lt;value&gt;)
     *      .add()
     * .generator(&lt;class&gt;)
     *      .prop(&lt;name&gt;, &lt;value&gt;)
     *      .prop(&lt;name&gt;, &lt;value&gt;)
     *      .add()
     *      .build()</code></pre>
     * @param generatorClassName the classname of the wadl generator to configure
     * @return an instance of {@link WadlGeneratorConfigDescriptionBuilder}.
     */
    public static WadlGeneratorConfigDescriptionBuilder generator( String generatorClassName ) {
        return new WadlGeneratorConfigDescriptionBuilder().generator( generatorClassName );
    }

    /**
     * Start to build an instance of {@link WadlGeneratorConfig}:
     * <pre><code>generator(&lt;generator&gt;).generator(&lt;generator&gt;).build()</code></pre>
     * @param generator the configured wadl generator
     * @return an instance of {@link WadlGeneratorConfigBuilder}.
     */
    public static WadlGeneratorConfigBuilder generator( WadlGenerator generator ) {
        return new WadlGeneratorConfigBuilder().generator( generator );
    }
    
    public static class WadlGeneratorConfigDescriptionBuilder {
        
        private List<WadlGeneratorDescription> _descriptions;
        private WadlGeneratorDescription _description;

        public WadlGeneratorConfigDescriptionBuilder() {
            _descriptions = new ArrayList<WadlGeneratorDescription>();
        }
        
        public WadlGeneratorConfigDescriptionBuilder generator( String generatorClassName ) {
            _description = new WadlGeneratorDescription();
            _description.setClassName( generatorClassName );
            return this;
        }
        
        public WadlGeneratorConfigDescriptionBuilder prop( String propName, String propValue ) {
            if ( _description.getProperties() == null ) {
                _description.setProperties( new Properties() );
            }
            _description.getProperties().put( propName, propValue );
            return this;
        }

        public WadlGeneratorConfigDescriptionBuilder add() {
            if ( _description == null ) {
                throw new NullPointerException( "Before you add a generator you must first" +
                		" configure it via #generator(String) and #prop(String,String).\n" +
                		"Usage: generator(<class>).prop(<name>, <value>).prop(<name>, <value>).add().build()" );
            }
            _descriptions.add( _description );
            _description = null;
            return this;
        }

        public WadlGeneratorConfig build() {
            if ( _description != null ) {
                throw new RuntimeException( "There's still a wadl generator description configured but not added.\n" +
                        "Usage: generator(<class>).prop(<name>, <value>).prop(<name>, <value>).add().build()" );
            }
            return new WadlGeneratorConfig( _descriptions );
        }
        
    }
    
    public static class WadlGeneratorConfigBuilder {
        
        private List<WadlGenerator> _generators;

        public WadlGeneratorConfigBuilder() {
            _generators = new ArrayList<WadlGenerator>();
        }
        
        public WadlGeneratorConfigBuilder generator( WadlGenerator generator ) {
            if ( generator == null ) {
                throw new IllegalArgumentException( "The wadl generator must not be null." );
            }
            _generators.add( generator );
            return this;
        }

        public WadlGeneratorConfig build() {
            try {
                final WadlGenerator wadlGenerator = WadlGeneratorLoader.loadWadlGenerators( _generators );
                return new WadlGeneratorConfig( wadlGenerator );
            } catch ( Exception e ) {
                throw new RuntimeException( "Could not load wadl generators.", e );
            }
        }
        
    }
    
//    public static void main( String[] args ) {
//        test1();
//    }
//
//    private static void test3() {
//        final Pattern pattern = Pattern.compile( "((\\w+)=([^,]+))" );
//        final Matcher matcher = pattern.matcher( "test11=foo11,test12=foo12" );
//        // System.out.println( matcher.matches() );
//
//        while( matcher.find() ) {
//            System.out.println( matcher.group(2) );
//            System.out.println( matcher.group(3) );
//        }
//    }
//
//    private static void test2() {
//        final Pattern pattern = Pattern.compile( "((\\w+)\\[([^]]+)\\])" );
//        final Matcher matcher = pattern.matcher( "test[test11=foo11,test12=foo12];test2[test21]" );
//        // System.out.println( matcher.matches() );
//        
//        while( matcher.find() ) {
//            System.out.println( matcher.groupCount() );
//            System.out.println( matcher.group( 1 ) );
//            System.out.println( matcher.group( 2 ) );
//            System.out.println( matcher.group( 3 ) );
//        }
//    }
//
//    private static void test1() {
//        final Pattern pattern = Pattern.compile( "[\\w.]+\\[\\w+=[^,\\]]+(,\\w+=[^,\\]]+)*\\](?:\\s*;\\s*(?:[\\w.]+\\[\\w+=[^,\\]]+(,\\w+=[^,\\]]+)*\\]))*" );
//        // final Pattern pattern = Pattern.compile( "[\\w.]+\\[\\w+=[^,\\]]+\\]" );
//        // final Pattern pattern = Pattern.compile( "\\w+\\[\\w+=[^,\\]]+(,\\w+=[^,]]+)*\\](?:\\s*;\\s*(?:\\w+\\[[^]]+\\]))*" );
//        final Matcher matcher = pattern.matcher( "test[test=foo,test1=foo1] ; test2[test2=bar]; test3[test3=baz,test31=baz31]" );
//        System.out.println( matcher.matches() );
//
//        final Matcher matcher2 = pattern.matcher( "com.sun.jersey.impl.wadl.generators.WadlGeneratorApplicationDoc" +
//        		"[applicationDocsFile=classpath:/src/main/api-doc/application-doc.xml]" );
//        System.out.println( matcher2.matches() );
//        
//    }
    

}
