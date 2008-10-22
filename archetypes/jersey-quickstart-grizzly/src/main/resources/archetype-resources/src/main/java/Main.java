
package $package;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;


public class Main {

    public static final URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();

    protected static SelectorThread startServer() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages", 
                "$package");

        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);     
        return threadSelector;
    }
    
    public static void main(String[] args) throws IOException {
        SelectorThread threadSelector = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...",
                BASE_URI));
        System.in.read();
        threadSelector.stopEndpoint();
        System.exit(0);
    }    
}
