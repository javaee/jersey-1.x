package com.sun.jersey.impl.template;

import com.sun.jersey.spi.template.TemplateProcessor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import javax.ws.rs.ext.Provider;

/**
 * 
 * @author Paul.Sandoz@Sun.Com
 */
@Provider
public class TestTemplateProcessor implements TemplateProcessor {

    public String resolve(String path) {
        if (!path.endsWith(".testp"))
            path = path + ".testp";
        
        URL u = this.getClass().getResource(path);
        if (u == null) return null;
        return path;
    }

    public void writeTo(String resolvedPath, Object model, OutputStream out) throws IOException {
        PrintStream ps = new PrintStream(out);
        ps.print("path=");
        ps.print(resolvedPath);
        ps.println();
        ps.print("model=");
        ps.print(model.toString());
        ps.println();
    }

}
