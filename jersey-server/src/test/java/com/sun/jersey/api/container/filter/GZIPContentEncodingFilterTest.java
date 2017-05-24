package com.sun.jersey.api.container.filter;

import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by vadyalex (at gmail).
 */
public class GZIPContentEncodingFilterTest {

    @Test
    public void preventAddingMultipleVaryAcceptEncodingHeaders() throws URISyntaxException {
        final WebApplicationImpl webApplication = new WebApplicationImpl();

        final ContainerRequest request = new ContainerRequest(
                new WebApplicationImpl(),
                "GET",
                new URI("base/uri"),
                new URI("request/uri"),
                new InBoundHeaders(),
                null
        );

        final ContainerResponse response = new ContainerResponse(
                webApplication,
                request,
                createContainerResponseWriter()
        );

        Assert.assertTrue(response.getHttpHeaders().isEmpty());

        final GZIPContentEncodingFilter filter = new GZIPContentEncodingFilter();

        filter.filter(request, response);
        filter.filter(request, response);
        filter.filter(request, response);

        Assert.assertNotNull(response.getHttpHeaders());
        Assert.assertTrue(response.getHttpHeaders().containsKey(HttpHeaders.VARY));
        Assert.assertEquals(1, response.getHttpHeaders().get(HttpHeaders.VARY).size());
    }

    private ContainerResponseWriter createContainerResponseWriter() {
        return new ContainerResponseWriter() {
            @Override
            public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException {
                return new ByteArrayOutputStream();
            }

            @Override
            public void finish() throws IOException {

            }
        };
    }


}
