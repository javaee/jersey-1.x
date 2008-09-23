
import java.net.URI;
import java.util.HashMap;
import javax.ws.rs.core.UriBuilder;
import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

package com.sun.jersey.samples.mandel {

object MandelServer {
    private def getPort(defaultPort : int) = {
        val port = System.getenv("JERSEY_HTTP_PORT");

        if (null != port)
            try  {
            Integer.parseInt(port);
            } catch {
                case ex: NumberFormatException => defaultPort;
            }
        else
            defaultPort;
    }

    private def getBaseURI() = {
        UriBuilder.fromUri("http://localhost/").port(getPort(9998)).
            path("mandelbrot").build();
    }

    val BASE_URI = getBaseURI();

    def startServer() = {
        val initParams = new HashMap[String, String]();
        initParams.put("com.sun.jersey.config.property.packages",
                "com.sun.jersey.samples.mandel");

        System.out.println("Starting grizzly...");
        GrizzlyWebContainerFactory.create(BASE_URI, initParams);
    }

    def main(args: Array[String]) {
        val selectorThread = startServer();

        println("Server running");
        println("Visit: " + BASE_URI + "/(-2.2,-1.2),(0.8,1.2)");
        println("The query parameter 'limit' specifies the limit on number of iterations");
        println("to determine if a point on the complex plain belongs to the mandelbrot set");
        println("The query parameter 'imageSize' specifies the maximum size of the image");
        println("in either the horizontal or virtical direction");
        println("Hit return to stop...");
        System.in.read();
        println("Stopping server");
        selectorThread.stopEndpoint();
        println("Server stopped");
        System.exit(0);
    }
}

}
