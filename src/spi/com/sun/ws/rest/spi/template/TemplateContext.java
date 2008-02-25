/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ws.rest.spi.template;

import com.sun.ws.rest.impl.template.*;
import java.util.Set;

/**
 * The context for getting template processors.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface TemplateContext {
    
    /**
     * Get the set of template processors.
     * 
     * @return the set of template processors.
     */
    Set<TemplateProcessor> getTemplateProcessors();
}