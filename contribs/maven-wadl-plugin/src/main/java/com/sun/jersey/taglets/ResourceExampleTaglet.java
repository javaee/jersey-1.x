/*
 * $Id: $ (c)
 * Copyright 2008 freiheit.com technologies GmbH
 *
 * Created on Jun 7, 2008
 *
 * This file contains unpublished, proprietary trade secret information of
 * freiheit.com technologies GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * freiheit.com technologies GmbH.
 */
package com.sun.jersey.taglets;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Jun 7, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class ResourceExampleTaglet implements Taglet {
    
    private static final Logger LOG = Logger.getLogger( ResourceExampleTaglet.class.getName() );
    
    /**
     * Register this Taglet.
     * @param tagletMap  the map to register this tag to.
     */
    public static void register( Map<String, Taglet> tagletMap ) {
        
        LOG.info( "Registering taglet..." );
        final ResourceExampleTaglet tag = new ResourceExampleTaglet();
        final Taglet t = tagletMap.remove( tag.getName() );
        if ( t != null ) {
            LOG.info( "Removed taglet " + t );
        }
        tagletMap.put( tag.getName(), tag );
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#getName()
     */
    public String getName() {
        return "resource.example";
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#inConstructor()
     */
    public boolean inConstructor() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#inField()
     */
    public boolean inField() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#inMethod()
     */
    public boolean inMethod() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#inOverview()
     */
    public boolean inOverview() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#inPackage()
     */
    public boolean inPackage() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#inType()
     */
    public boolean inType() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#isInlineTag()
     */
    public boolean isInlineTag() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#toString(com.sun.javadoc.Tag)
     */
    public String toString( Tag tag ) {
        LOG.info( "Invoked with tag: " + print( tag ) );
        final Tag[] inlineTags = tag.inlineTags();
        if ( inlineTags != null ) {
            for ( Tag inlineTag : inlineTags ) {
                if ( "@link".equals( inlineTag.name() ) ) {
                    LOG.info( "Have link: " + print( inlineTag ) );
                    final SeeTag linkTag = (SeeTag) inlineTag;
                    return handleLinkTag( linkTag );
                }
            }
        }
        return "foo";
    }

    /* (non-Javadoc)
     * @see com.sun.tools.doclets.Taglet#toString(com.sun.javadoc.Tag[])
     */
    public String toString( Tag[] tags ) {
        if ( tags != null && tags.length == 1 ) {
            return toString( tags[0] );
        }
        return null;
    }

    private String handleLinkTag( final SeeTag linkTag ) {
        final MemberDoc referencedMember = linkTag.referencedMember();
        if ( !referencedMember.isStatic() ) {
            LOG.warning( "Referenced member of @link "+ print( linkTag ) +" is not static." +
            		" Right now only references to static members are supported." );
            return null;
        }
        
        /* Get referenced example bean
         */
        final ClassDoc containingClass = referencedMember.containingClass();
        final Object object; 
        try {
            Field declaredField = Class.forName( containingClass.qualifiedName() ).getDeclaredField( referencedMember.name() );
            if ( referencedMember.isFinal() ) {
                declaredField.setAccessible( true );
            }
            object = declaredField.get( null );
            LOG.log( Level.FINE, "Got object " + object );
        } catch ( Exception e ) {
            LOG.log( Level.SEVERE, "Could not get field " + referencedMember.qualifiedName(), e );
            return null;
        }
        
        /* marshal the bean to xml
         */
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance( object.getClass() );
            final StringWriter stringWriter = new StringWriter();
            final Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            marshaller.marshal( object, stringWriter );
            final String result = stringWriter.getBuffer().toString();
            LOG.log( Level.FINE, "Got marshalled output:\n" + result );
            return result;
        } catch ( Exception e ) {
            LOG.log( Level.SEVERE, "Could serialize bean to xml: " + object, e );
            return null;
        }
    }

    private String print( Tag tag ) {
        final StringBuilder sb = new StringBuilder();
        sb.append( tag.getClass() ).append( "[" );
        sb.append( "firstSentenceTags=" ).append( toCSV( tag.firstSentenceTags() ) );
        sb.append( ", inlineTags=" ).append( toCSV( tag.inlineTags() ) );
        sb.append( ", kind=" ).append( tag.kind() );
        sb.append( ", name=" ).append( tag.name() );
        sb.append( ", text=" ).append( tag.text() );
        sb.append( "]" );
        return sb.toString();
    }

    static <T> String toCSV( Tag[] items ) {
        if ( items == null ) {
            return null;
        }
        return toCSV( Arrays.asList( items ) );
    }
    
    static <I> String toCSV( Collection<Tag> items ) {
        return toCSV( items, ", ", null );
    }
    
    static <I> String toCSV( Collection<Tag> items, String separator, String delimiter ) {
        if ( items == null ) {
            return null;
        }
        if ( items.isEmpty() ) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for ( final Iterator<Tag> iter = items.iterator(); iter.hasNext(); ) {
            if ( delimiter != null ) {
                sb.append( delimiter );
            }
            final Tag item = iter.next();
            sb.append( item.name() );
            if ( delimiter != null ) {
                sb.append( delimiter );
            }
            if ( iter.hasNext() ) {
                sb.append( separator );
            }
        }
        return sb.toString();
    }

}
