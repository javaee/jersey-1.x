import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.Adapter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;
import com.sun.jersey.samples.groovy.GroovyResource;

import javax.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

adapter = ContainerFactory.createContainer(
                Adapter.class,
                GroovyResource.class)

baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build()
server = GrizzlyServerFactory.create(baseUri, adapter)

System.out.println(String.format("Jersey app started with WADL available at "
        + "%sapplication.wadl\nTry out %sgroovy\nHit enter to stop it...",
        baseUri, baseUri));
System.in.read()

server.stopEndpoint()