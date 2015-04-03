package com.sun.jersey.server.api.container.filter;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * @author Mark Fulton
 */
public class GZIPContentEncodingFilterTest {
    @Test
    public void containerResponseFilterSupportsObjectResponseHeaderValues() {
        WebApplicationImpl webApplication = new WebApplicationImpl();
        String method = "PUT";
        URI baseUri = URI.create("base/uri");
        URI requestUri = URI.create("request/uri");
        InBoundHeaders headers = new InBoundHeaders();
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        ByteArrayInputStream entity = new ByteArrayInputStream("entity".getBytes(StandardCharsets.UTF_8));
        ContainerRequest request = new ContainerRequest(webApplication, method, baseUri, requestUri, headers, entity);
        ContainerResponse response = new ContainerResponse(webApplication, request, createResponseWriter());
        response.getHttpHeaders().putSingle(HttpHeaders.CONTENT_ENCODING, ContentEncoding.GZIP);
        new GZIPContentEncodingFilter().filter(request, response);
    }

    private ContainerResponseWriter createResponseWriter() {
        return new ContainerResponseWriter() {
            @Override
            public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response)
                    throws IOException {
                return new ByteArrayOutputStream();
            }

            @Override
            public void finish() throws IOException {
            }
        };
    }

    /**
     * @author Mark Fulton
     */
    private enum ContentEncoding {
        GZIP,
        COMPRESS,
        DEFLATE,
        IDENTITY;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
