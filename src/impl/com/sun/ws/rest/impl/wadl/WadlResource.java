package com.sun.ws.rest.impl.wadl;

import com.sun.research.ws.wadl.Application;
import com.sun.ws.rest.api.model.AbstractResource;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@ProduceMime({"application/vnd.sun.wadl+xml", "application/xml"})
public final class WadlResource {
    private final Application a;
    
    public WadlResource(Set<AbstractResource> rootResources) {
        // TODO serialize JAXB bean to byte[]
        // no need to serilize WADL every time it is requested
        this.a = WadlGenerator.generate(rootResources);
    }
    
    public @GET Application getWadl() {
        return a;
    }
}
