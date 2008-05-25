package com.sun.jersey.impl.wadl;

import com.sun.research.ws.wadl.Application;
import com.sun.jersey.api.model.AbstractResource;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

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
    
    public synchronized @GET Application getWadl(@Context UriInfo uriInfo) {
        if (a.getResources().getBase()==null)
            a.getResources().setBase(uriInfo.getBaseUri().toString());
        return a;
    }
}
