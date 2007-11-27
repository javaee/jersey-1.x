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

/*
 * MapResource.java
 *
 * Created on November 21, 2007, 11:12 AM
 *
 */

package com.sun.ws.rest.samples.entityprovider.resources;

import com.sun.ws.rest.spi.resource.Singleton;
import java.util.Hashtable;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;

/**
 * A resource that manages a map of name/value pairs.
 */
@Singleton
@Path("data")
public class MapResource {
    
    private static Hashtable<String, String> data = new Hashtable<String, String>();
    
    /** Creates a new instance of MapResource */
    public MapResource() {
        data.put("foo", "bar");
    }
    
    @HttpMethod("GET")
    @ProduceMime("text/html")
    public Hashtable<String, String> getData() {
        return data;
    }
    
    @HttpMethod("POST")
    @ProduceMime("text/html")
    public Hashtable<String, String> updateDataItem(NameValuePair item) {
        if (item.getName()!=null) {
            if (item.getValue()==null || item.getValue().length()==0)
                data.remove(item.getName());
            else
                data.put(item.getName(),item.getValue());
        }
        return data;
    }
}
