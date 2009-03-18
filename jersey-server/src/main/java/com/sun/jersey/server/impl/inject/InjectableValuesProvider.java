package com.sun.jersey.server.impl.inject;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.inject.Injectable;
import java.util.List;
import javax.ws.rs.WebApplicationException;


/**
 * A hold of a list of injectable that obtains the injectable values
 * from that list.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectableValuesProvider {

    private final List<AbstractHttpContextInjectable> is;

    /**
     * Create a new instance given a list of injectable.
     * 
     * @param is the list of injectable.
     */
    public InjectableValuesProvider(List<Injectable> is) {
        super();
        this.is = AbstractHttpContextInjectable.transform(is);
    }

    /**
     * Get the injectable values.
     *
     * @param context the http contest.
     * @return the injectable values. Each element in the object array
     *         is a value obtained from the injectable at the list index
     *         that is the element index.
     */
    public Object[] getInjectableValues(HttpContext context) {
        final Object[] params = new Object[is.size()];
        try {
            int index = 0;
            for (AbstractHttpContextInjectable i : is) {
                params[index++] = i.getValue(context);
            }
            return params;
        } catch (WebApplicationException e) {
            throw e;
        } catch (ContainerException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ContainerException("Exception obtaining parameters", e);
        }
    }
}