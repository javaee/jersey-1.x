package com.sun.jersey.samples.sparklines;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class Interval extends IntegerList {
    public Interval(String s) {
        super(s);
        if (size() != 2)
            throw new WebApplicationException(400);
        if (upper() < lower())
            throw new WebApplicationException(400);
    }
    
    public int lower() {
        return get(0);
    }

    public int upper() {
        return get(1);
    }

    public int width() {
        return upper() - lower();
    }
    
    public boolean contains(List<Integer> data) {
        if (Collections.min(data) < lower() ||
                Collections.max(data) > upper()) {
            return false;
        } else
            return true;
    }
}