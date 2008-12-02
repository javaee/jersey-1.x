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
package com.sun.jersey.wadl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.sun.jersey.wadl.util.Loader;

/**
 * This is a base class for mojos that need to access the project classpath.
 * 
 */
public abstract class AbstractMojoProjectClasspathSupport extends AbstractMojo {

    /**
     * Additional dependencies that shall be made available when running this
     * goal. Use this when you get e.g. <code>ClassNotFoundException</code>s.
     * 
     * @parameter property="dependencies"
     */
    private List<com.sun.jersey.wadl.Dependency> _dependencies;
    
    /** @parameter expression="${project}" */
    private org.apache.maven.project.MavenProject mavenProject;
    
    /** @component */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    /** @component */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**@parameter expression="${localRepository}" */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

    /** @parameter expression="${project.remoteArtifactRepositories}" */
    private java.util.List<ArtifactRepository> remoteRepositories;

    public final void execute() throws MojoExecutionException {

        final List<String> classpathElements;
        try {
            classpathElements = getClasspathElements( mavenProject, _dependencies );
        } catch ( Exception e ) {
            getLog().error( e );
            throw new MojoExecutionException( "Could not create the list of classpath elements.", e );
        }
        
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final ClassLoader ncl = new Loader( classpathElements.toArray( new String[0] ), this.getClass().getClassLoader() );
        Thread.currentThread().setContextClassLoader(ncl);
        try {
            executeWithClasspath( classpathElements );
        } catch( MojoExecutionException e ) {
            throw e;
        } catch(Exception e) {
            getLog().error( e );
            throw new MojoExecutionException( "Could not execute mojo", e );
        }
        finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
    
    protected abstract void executeWithClasspath( List<String> classpathElements ) throws MojoExecutionException;

    /**
     * Create a list of classpath elements including declared build dependencies, the build
     * output directory and additionally configured dependencies.
     * @param mavenProject
     * @param additionalDependencies
     * @return a list of classpath elements
     * @throws DependencyResolutionRequiredException
     * @throws ArtifactResolutionException
     * @throws ArtifactNotFoundException
     * @author Martin Grotzke
     */
    protected List<String> getClasspathElements( final MavenProject mavenProject,
            final List<com.sun.jersey.wadl.Dependency> additionalDependencies )
            throws DependencyResolutionRequiredException,
            ArtifactResolutionException, ArtifactNotFoundException {
        
        final List<String> paths = new ArrayList<String>();
        
        /* Add maven compile classpath elements
         */
        @SuppressWarnings("unchecked")
        final List<String> compileClasspathElements = mavenProject.getCompileClasspathElements();
        paths.addAll( compileClasspathElements );
        
        /* Add build dependencies as classpath elements
         */
        @SuppressWarnings("unchecked")
        final Collection<Dependency> dependencies = mavenProject.getDependencies();
        if ( dependencies != null ) {
            for ( Dependency dependency : dependencies ) {
                if ( dependency.getSystemPath() != null ) {
                    getLog().debug( "Adding dependency with systemPath " + dependency.getSystemPath() );
                    paths.add( dependency.getSystemPath() );
                }
                else {
                    final Artifact artifact = artifactFactory.createArtifactWithClassifier(
                            dependency.getGroupId(), dependency.getArtifactId(),
                            dependency.getVersion(), dependency.getType(), dependency.getClassifier() );
                    resolver.resolve( artifact, remoteRepositories, localRepository );
                    getLog().debug( "Adding artifact " + artifact.getFile().getPath() );
                    paths.add( artifact.getFile().getPath() );
                }
            }
        }
        
        /* Add additional dependencies
         */
        if ( additionalDependencies != null ) {
            for ( com.sun.jersey.wadl.Dependency dependency : additionalDependencies ) {
                if ( dependency.getSystemPath() != null ) {
                    getLog().debug( "Adding additional dependency with systemPath " + dependency.getSystemPath() );
                    paths.add( dependency.getSystemPath() );
                }
                else {
                    final Artifact artifact = artifactFactory.createArtifactWithClassifier(
                            dependency.getGroupId(), dependency.getArtifactId(),
                            dependency.getVersion(), "jar", null );
                    resolver.resolve( artifact, remoteRepositories, localRepository );
                    getLog().debug( "Adding additional artifact " + artifact.getFile().getPath() );
                    paths.add( artifact.getFile().getPath() );
                }
            }
        }
        
        return paths;
    }
    
    /**
     * @param dependencies the dependencies to set
     * @author Martin Grotzke
     */
    public void setDependencies( List<com.sun.jersey.wadl.Dependency> dependencies ) {
        _dependencies = dependencies;
    }
    
}
