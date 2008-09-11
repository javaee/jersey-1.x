package com.sun.jersey.samples.sparklines;

import java.util.ArrayList;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class IntegerList extends ArrayList<Integer> {
    public IntegerList(String s) {
        super();

        for (String v : s.split(",")) {
            try {
                add(Integer.parseInt(v.trim()));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new WebApplicationException(400);
            }
        }
        if (isEmpty()) 
            throw new WebApplicationException(400);
    }
}