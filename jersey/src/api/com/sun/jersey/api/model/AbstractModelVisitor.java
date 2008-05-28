/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.sun.jersey.api.model;

/**
 * The interface was originatelly created to allow implementing
 * validators of the abstract resource model. A second use case
 * would be WADL generation. 
 * 
 * @author japod
 */
public interface AbstractModelVisitor {

    void visitAbstractResource(AbstractResource resource);

    void visitAbstractField(AbstractField field);
    
    void visitAbstractSetterMethod(AbstractSetterMethod setterMethod);
    
    void visitAbstractResourceMethod(AbstractResourceMethod method);

    void visitAbstractSubResourceMethod(AbstractSubResourceMethod method);

    void visitAbstractSubResourceLocator(AbstractSubResourceLocator locator);

    void visitAbstractResourceConstructor(AbstractResourceConstructor constructor);
}
