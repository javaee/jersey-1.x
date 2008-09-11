package com.sun.jersey.samples.sparklines;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.imageio.ImageIO;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Provider
@Produces("image/png")
public class RenderedImageWriter implements MessageBodyWriter<RenderedImage> {

    public boolean isWriteable(Class<?> c, Type t, Annotation[] as, MediaType mt) {
        return RenderedImage.class.isAssignableFrom(c);
    }

    public long getSize(RenderedImage r, Class<?> c, Type t, Annotation[] as, MediaType mt) {
        return -1;
    }

    public void writeTo(RenderedImage r, Class<?> c, Type t, Annotation[] as, MediaType mt,
            MultivaluedMap<String, Object> h, OutputStream out) throws IOException, WebApplicationException {
        ImageIO.write(r, "png", out);
    }
}
