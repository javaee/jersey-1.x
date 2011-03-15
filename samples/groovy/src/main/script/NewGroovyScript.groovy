import com.sun.jersey.api.container.ContainerFactory
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory
import com.sun.jersey.samples.groovy.GroovyResource
import javax.ws.rs.core.UriBuilder
import org.glassfish.grizzly.http.server.HttpHandler

handler = ContainerFactory.createContainer(
                HttpHandler.class,
                GroovyResource.class)

baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build()
server = GrizzlyServerFactory.create(baseUri, handler)

System.out.println(String.format("Jersey app started with WADL available at "
        + "%sapplication.wadl\nTry out %sgroovy\nHit enter to stop it...",
        baseUri, baseUri));
System.in.read()

server.stop()