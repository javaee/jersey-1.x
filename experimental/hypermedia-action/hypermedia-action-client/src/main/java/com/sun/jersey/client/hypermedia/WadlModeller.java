/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.client.hypermedia;

import org.jvnet.ws.wadl.*;
import org.jvnet.ws.wadl2java.ast.FaultNode;
import org.jvnet.ws.wadl2java.ast.MethodNode;
import org.jvnet.ws.wadl2java.ast.RepresentationNode;
import org.jvnet.ws.wadl2java.ast.ResourceNode;
import org.jvnet.ws.wadl2java.ast.ResourceTypeNode;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.jvnet.ws.wadl2java.ElementResolver;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Processes a WADL file and returns an AST.
 *
 * @author Marc.Hadley@sun.com
 * @author Santiago.PericasGeertsen@sun.com
 */
public class WadlModeller {
    
    private S2JJAXBModel s2jModel;
    private ElementResolver idMap;
    private Map<String, ResourceTypeNode> ifaceMap;
    private List<String> processedDocs;
    private Unmarshaller u;
    private SchemaCompiler s2j;

    public WadlModeller() {
        processedDocs = new ArrayList<String>();
    }

    public ElementResolver getIdMap() {
        return idMap;
    }

    public Map<String, ResourceTypeNode> getIfaceMap() {
        return ifaceMap;
    }

    public List<ResourceNode> process(URI rootDesc, InputStream is)
            throws JAXBException, IOException {
        // read in root WADL file
        JAXBContext jbc = JAXBContext.newInstance("org.jvnet.ws.wadl", 
                this.getClass().getClassLoader() );
        u = jbc.createUnmarshaller();
        s2j = new SchemaCompilerImpl();
        idMap = new ElementResolver();
        ifaceMap = new HashMap<String, ResourceTypeNode>();
        Application a = processDescription(rootDesc, is);
        return buildAst(a, rootDesc);
    }
    
    public Application processDescription(URI desc, InputStream is)
            throws JAXBException, IOException {
        // check for files that have already been processed to prevent loops
        if (processedDocs.contains(desc.toString()))
            return null;
        processedDocs.add(desc.toString());
        
        // read in WADL file
        Application a = (Application)u.unmarshal(is);
        
        // process embedded schemas
        Grammars g = a.getGrammars();
        if (g != null) {
            for (Include i: g.getInclude()) {
                URI incl = desc.resolve(i.getHref());
                if (processedDocs.contains(incl.toString()))
                    continue;
                processedDocs.add(incl.toString());
                InputSource input = new InputSource(incl.toURL().openStream());
                input.setSystemId(incl.toString());
                s2j.parseSchema(input);
            }
            int embeddedSchemaNo = 0; // used to generate unique system ID
            for (Object any: g.getAny()) {
                if (any instanceof Element) {
                    Element element = (Element)any;
                    s2j.parseSchema(desc.toString()+"#schema"+
                            Integer.toString(embeddedSchemaNo), element);
                    embeddedSchemaNo++;
                }
            }
        }
        
        buildIDMap(a, desc);
        return a;
    }

    @SuppressWarnings("unchecked")
    protected void buildIDMap(Application a, URI desc)
            throws JAXBException, IOException {
        // process globally declared items
        for (Object child: a.getResourceTypeOrMethodOrRepresentation()) {
            if (child instanceof Method)
                extractMethodIds((Method)child, desc);
            else if (child instanceof ResourceType)
                extractResourceTypeIds((ResourceType)child, desc);
            else if (child instanceof Representation)
                extractRepresentationId((Representation)child, desc);
            else
                extractParamId((Param)child, desc);
        }
        
        // process resource hierarchy
        if (a.getResources() != null)
            for (Resources rs: a.getResources())
                for (Resource r: rs.getResource())
                    extractResourceIds(r, desc);
    }
    
    /**
     * Adds the object to the ID map if it is identified and process any 
     * file pointed to by href.
     * @param desc The URI of the current file being processed, used
     * when resolving relative paths in href
     * @param id The identifier of o or null if o isn't identified
     * @param href A link to a another element, the document in which 
     * the element resides will be recursively processed
     * @param o The object that is being identified or from which the link occurs
     * @return a unique identifier for the element or null if not identified
     * @throws javax.xml.bind.JAXBException if the WADL file is invalid or if 
     * the code generator encounters a problem.
     * @throws java.io.IOException if the specified WADL file cannot be read.
     */
    protected String processIDHref(URI desc, String id, String href, Object o)
            throws JAXBException, IOException {
        String uniqueId = idMap.addReference(desc, id, o);
        if (href != null && href.startsWith("#") == false) {
            throw new IllegalStateException("The WADL at " + desc +
                    " was expected to be self-contained but includes a" +
                    " reference to " + href);
        }
        return uniqueId;
    }
    
    /**
     * Extract the id from a representation element and add to the
     * representation map.
     * @param file the URI of the current WADL file being processed
     * @param r the representation element
     * @throws javax.xml.bind.JAXBException if the WADL file is invalid or if 
     * the code generator encounters a problem.
     * @throws java.io.IOException if the specified WADL file cannot be read.
     */
    protected void extractRepresentationId(Representation r, URI file)
            throws JAXBException, IOException {
        processIDHref(file, r.getId(), r.getHref(), r);
        for (Param p: r.getParam())
            extractParamId(p, file);
    }
    
    /**
     * Extract the id from a param element and add to the
     * representation map.
     * @param file the URI of the current WADL file being processed
     * @param p the param element
     * @throws javax.xml.bind.JAXBException if the WADL file is invalid or if 
     * the code generator encounters a problem.
     * @throws java.io.IOException if the specified WADL file cannot be read.
     */
    protected void extractParamId(Param p, URI file)
            throws JAXBException, IOException {
        processIDHref(file, p.getId(), p.getHref(), p);
    }
    
    /**
     * Extract the id from a method element and add to the
     * method map. Also extract the ids from any contained representation or
     * fault elements.
     * @param file the URI of the current WADL file being processed
     * @param m the method element
     * @throws javax.xml.bind.JAXBException if the WADL file is invalid or if 
     * the code generator encounters a problem.
     * @throws java.io.IOException if the specified WADL file cannot be read.
     */
    protected void extractMethodIds(Method m, URI file)
            throws JAXBException, IOException {
        processIDHref(file, m.getId(), m.getHref(), m);

        if (m.getRequest() != null) {
            for (Param p: m.getRequest().getParam())
                extractParamId(p, file);
            for (Representation r: m.getRequest().getRepresentation())
                extractRepresentationId(r, file);
        }
        for (Response resp: m.getResponse()) {
            for (Param p: resp.getParam())
                extractParamId(p, file);
            for (Representation r: resp.getRepresentation()) {
                extractRepresentationId(r, file);
            }
        }
    }
    
    /**
     * Extract the id from a resource element and add to the
     * resource map then recurse into any contained resources.
     * Also extract the ids from any contained param, method and its
     * representation or fault elements.
     * @param file the URI of the current WADL file being processed
     * @param r the resource element
     * @throws javax.xml.bind.JAXBException if the WADL file is invalid or if 
     * the code generator encounters a problem.
     * @throws java.io.IOException if the specified WADL file cannot be read.
     */
    protected void extractResourceIds(Resource r, URI file)
            throws JAXBException, IOException {
        processIDHref(file, r.getId(), null, r);
        for (String type: r.getType()) {
            processIDHref(file, null, type, r);
        }
        for (Param p: r.getParam())
            extractParamId(p, file);
        for (Object child: r.getMethodOrResource()) {
            if (child instanceof Method)
                extractMethodIds((Method)child, file);
            else if (child instanceof Resource)
                extractResourceIds((Resource)child, file);
        }
    }
    
    /**
     * Extract the id from a resource_type element and add to the
     * resource map.
     * Also extract the ids from any contained method and its param,
     * representation or fault elements.
     * @param file the URI of the current WADL file being processed
     * @param r the resource_type element
     * @throws javax.xml.bind.JAXBException if the WADL file is invalid or if 
     * the code generator encounters a problem.
     * @throws java.io.IOException if the specified WADL file cannot be read.
     */
    protected void extractResourceTypeIds(ResourceType r, URI file)
            throws JAXBException, IOException {
        String id = processIDHref(file, r.getId(), null, r);
        if (id != null)
            ifaceMap.put(id, null);
        for (Param p: r.getParam())
            extractParamId(p, file);
        for (Object child: r.getMethodOrResource()) {
            if (child instanceof Method)
                extractMethodIds((Method)child, file);
            else if (child instanceof Resource)
                extractResourceIds((Resource)child, file);
        }
    }
        
    /**
     * Build an abstract tree from an unmarshalled WADL file
     * @param a the application element of the root WADL file
     * @param rootFile the URI of the root WADL file. Other WADL files might be
     * included by reference.
     * @return the resource elements that correspond to the roots of the resource trees
     */
    protected List<ResourceNode> buildAst(Application a, URI rootFile) {
        // process resource types in two steps:
        // (i) process resource types in terms of methods
        for (String ifaceId: ifaceMap.keySet()) {
            buildResourceType(ifaceId, a);
        }
        // (ii) process resource type child resources (which may reference
        // resource types located in (i)
        for (String ifaceId: ifaceMap.keySet()) {
            buildResourceTypeTree(ifaceId, a);
        }
        
        List<Resources> rs = a.getResources();
        List<ResourceNode> ns = new ArrayList<ResourceNode>();
        for (Resources r: rs) {
            ResourceNode rootResourcesNode = new ResourceNode(a, r);
            for (Resource child: r.getResource()) {
                buildResourceTree(rootResourcesNode, child, rootFile);
            }
            ns.add(rootResourcesNode);
        }
        
        return ns;
    }
    
    /**
     * Build an abstract resource type based on the methods of a resource type 
     * in a WADL file
     * @param ifaceId the identifier of the resource type
     * @param a the application element of the root WADL file
     */
    protected void buildResourceType(String ifaceId, Application a) {
        try {
            URI file = new URI(ifaceId.substring(0,ifaceId.indexOf('#')));
            ResourceType type = (ResourceType)idMap.get(ifaceId);
            ResourceTypeNode node = new ResourceTypeNode(type, file, idMap);
            for (Object child: type.getMethodOrResource()) {
                if (child instanceof Method)
                    addMethodToResourceType(node, (Method)child, file);
            }
            ifaceMap.put(ifaceId, node);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }        
    }
    
    /**
     * Build an abstract resource type tree based on the child resources of a 
     * resource type in a WADL file
     * @param ifaceId the identifier of the resource type
     * @param a the application element of the root WADL file
     */
    protected void buildResourceTypeTree(String ifaceId, Application a) {
        try {
            URI file = new URI(ifaceId.substring(0,ifaceId.indexOf('#')));
            ResourceType type = (ResourceType)idMap.get(ifaceId);
            ResourceTypeNode node = ifaceMap.get(ifaceId);
            for (Object child: type.getMethodOrResource()) {
                if (child instanceof Resource)
                    addResourceToResourceType(node, (Resource)child, file);
            }
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }        
    }
    
    /**
     * Add a resource and (recursively) its children to a tree starting at the parent.
     * Follow references to resources across WADL file boundaries
     * @param parent the parent resource in the tree being built
     * @param resource the WADL resource to process
     * @param file the URI of the current WADL file being processed
     */
    protected void buildResourceTree(ResourceNode parent, 
            Resource resource, URI file) {
        if (resource != null) {
            ResourceNode n = parent.addChild(resource, file, idMap);
            for (String type: resource.getType()) {
                addTypeToResource(n, type, file);
            }
            for (Object child: resource.getMethodOrResource()) {
                if (child instanceof Resource) {
                    Resource childResource = (Resource)child;
                    buildResourceTree(n, childResource, file);
                } else if (child instanceof Method) {
                    Method m = (Method)child;
                    addMethodToResource(n, m, file);
                }
            }
        }
    }
    
    /**
     * Add a type to a resource.
     * Follow references to types across WADL file boundaries
     * @param href the identifier of the resource_type element to process
     * @param resource the resource
     * @param file the URI of the current WADL file being processed
     */
    protected void addTypeToResource(ResourceNode resource, String href, 
            URI file) {
        // dereference resource
        file = getReferencedFile(file, href);
        ResourceTypeNode n = ifaceMap.get(file.toString()
                + href.substring(href.indexOf('#')));
        
        if (n != null) {
            resource.addResourceType(n);
        } 
    }
    
    /**
     * Add a method to a resource type.
     * Follow references to methods across WADL file boundaries
     * @param method the WADL method element to process
     * @param resource the resource type
     * @param file the URI of the current WADL file being processed
     */
    protected void addMethodToResourceType(ResourceTypeNode resource, Method method, 
            URI file) {
        String href = method.getHref();
        if (href != null && href.length() > 0) {
            // dereference resource
            file = getReferencedFile(file, href);
            method = idMap.resolve(file, href, Method.class);
        }
        if (method != null) {
            MethodNode n = new MethodNode(method, resource);
            Request request = method.getRequest();
            if (request != null) {
                for (Param p: request.getParam()) {
                    href=p.getHref();
                    if (href != null && href.length() > 0) {
                        // dereference param
                        file = getReferencedFile(file, href);
                        p = idMap.resolve(file, href, Param.class);
                    }
                    if (p != null)
                        n.getQueryParameters().add(p);
                }
                for (Representation r: request.getRepresentation()) {
                    addRepresentation(n.getSupportedInputs(), r, file);
                }
            }
            for (Response response: method.getResponse()) {
                boolean isFault = isFaultResponse(response);
                for (Representation o: response.getRepresentation()) {
                    if (isFault) {
                        FaultNode fn = new FaultNode(o);
                        n.getFaults().add(fn);
                    } else {
                        addRepresentation(n.getSupportedOutputs(), o, file);
                    }
                }
            }
        }        
    }
    
    /**
     * Add a child resource to a resource type.
     * Follow references to resources across WADL file boundaries
     * @param resource the WADL resource element to process
     * @param type the parent resource type
     * @param file the URI of the current WADL file being processed
     */
    protected void addResourceToResourceType(ResourceTypeNode type, Resource resource, 
            URI file) {
        if (resource != null) {
            ResourceNode n = type.addChild(resource, file, idMap);
            for (String resourceType: resource.getType()) {
                addTypeToResource(n, resourceType, file);
            }
            for (Object child: resource.getMethodOrResource()) {
                if (child instanceof Resource) {
                    Resource childResource = (Resource)child;
                    buildResourceTree(n, childResource, file);
                } else if (child instanceof Method) {
                    Method m = (Method)child;
                    addMethodToResource(n, m, file);
                }
            }
        }
    }
    
    /**
     * Check if the supplied Response represents an error or not. If any
     * of the possible HTTP status values is >= 400 the Response is considered
     * to represent a fault.
     * @param response the response to check
     * @return true if the response represents a fault, false otherwise.
     */
    boolean isFaultResponse(Response response) {
        boolean isFault = false;
        for (long status: response.getStatus()) {
            if (status >= 400) {
                isFault = true;
                break;
            }
        }
        return isFault;
    }
    
    /**
     * Add a method to a resource.
     * Follow references to methods across WADL file boundaries
     * @param method the WADL method element to process
     * @param resource the resource
     * @param file the URI of the current WADL file being processed
     */
    protected void addMethodToResource(ResourceNode resource, Method method, 
            URI file) {
        String href = method.getHref();
        if (href != null && href.length() > 0) {
            // dereference resource
            file = getReferencedFile(file, href);
            method = idMap.resolve(file, href, Method.class);
        }
        if (method != null) {
            MethodNode n = new MethodNode(method, resource);
            Request request = method.getRequest();
            if (request != null) {
                for (Param p: request.getParam()) {
                    href=p.getHref();
                    if (href != null && href.length() > 0) {
                        // dereference param
                        file = getReferencedFile(file, href);
                        p = idMap.resolve(file, href, Param.class);
                    }
                    if (p != null) {
                        if (p.getStyle()==ParamStyle.HEADER)
                            n.getHeaderParameters().add(p);
                        else
                            n.getQueryParameters().add(p);
                    }                }
                for (Representation r: request.getRepresentation()) {
                    addRepresentation(n.getSupportedInputs(), r, file);
                }
            }
            for (Response response: method.getResponse()) {
                boolean isFault = isFaultResponse(response);
                for (Representation o: response.getRepresentation()) {
                    if (isFault) {
                        FaultNode fn = new FaultNode(o);
                        n.getFaults().add(fn);
                    } else {
                        addRepresentation(n.getSupportedOutputs(), o, file);
                    }
                }
            }
        }        
    }

    /**
     * Add a representation to a method's input or output list.
     * Follow references to representations across WADL file boundaries
     * @param list the list to add the representation to
     * @param representation the WADL representation element to process
     * @param file the URI of the current WADL file being processed
     */
    protected void addRepresentation(List<RepresentationNode> list,
            Representation representation, URI file) {
        String href = representation.getHref();
        if (href != null && href.length() > 0) {
            // dereference resource
            file = getReferencedFile(file, href);
            representation = idMap.resolve(file, href, Representation.class);
        }
        if (representation != null) {
            RepresentationNode n = new RepresentationNode(representation);
            list.add(n);
        }
    }
    
    /**
     * Get the referenced file, currentFile will be returned if href is a
     * fragment identifier, otherwise href is resolved against currentFile.
     * @param currentFile the uri of the file that contains the reference, used 
     * to provide a base for relative paths
     * @param href the reference
     * @return the URI of the referenced file
     */
    protected static URI getReferencedFile(URI currentFile, String href) {
        if (href.startsWith("#"))
            return currentFile;
        // href references another file
        URI ref = currentFile.resolve(href.substring(0, href.indexOf('#')));
        return ref;
    }
    
}
