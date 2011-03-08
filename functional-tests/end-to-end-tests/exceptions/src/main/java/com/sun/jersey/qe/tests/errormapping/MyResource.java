package com.sun.jersey.qe.tests.errormapping;

import com.sun.jersey.api.container.MappableContainerException;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Path("/myresource")
public class MyResource {
    
    @GET 
    @Produces("text/plain")
    public String getIt() {
        return "Hi there!";
    }

    @Path("runtime")
    @GET
    @Produces("text/plain")
    public String runtimeException(@QueryParam("ex") String className) 
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class c = Class.forName(className, true, this.getClass().getClassLoader());
        RuntimeException re = (RuntimeException)c.newInstance();
        if (true) throw re;
        return "Hi there!";
    }

    @Path("checked/ioexception")
    @GET
    @Produces("text/plain")
    public String checkedIOException() throws IOException {
        if (true) throw new IOException();
        return "Hi there!";
    }

    public static class MyException extends Exception {
    }

    @Path("checked/myexception")
    @GET
    @Produces("text/plain")
    public String checkedMyException() throws MyException {
        if (true) throw new MyException();
        return "Hi there!";
    }


    public static class MyMappedException extends Exception {
    }

    @Provider
    public static class MyMappedExceptionMapper implements ExceptionMapper<MyMappedException> {
        public Response toResponse(MyMappedException exception) {
            return Response.serverError().entity("Jersey mapped exception: " + exception.getClass().getName()).build();
        }
    }

    @Path("checked/mymappedexception")
    @GET
    @Produces("text/plain")
    public String checkedMyMappedException() throws MyMappedException {
        if (true) throw new MyMappedException();
        return "Hi there!";
    }


    public static class MyMappedRuntimeException extends RuntimeException {
    }

    @Provider
    public static class MyMappedRuntimeExceptionMapper implements ExceptionMapper<MyMappedRuntimeException> {
        public Response toResponse(MyMappedRuntimeException exception) {
            return Response.serverError().entity("Jersey mapped runtime exception: " + exception.getClass().getName()).build();
        }
    }

    @Path("checked/mymappedruntimeexception")
    @GET
    @Produces("text/plain")
    public String checkedMyMappedRuntimeException() {
        if (true) throw new MyMappedRuntimeException();
        return "Hi there!";
    }



    public static class MyMappedThrowingException extends Exception {
    }

    @Provider
    public static class MyMappedThrowingExceptionMapper implements ExceptionMapper<MyMappedThrowingException> {
        public Response toResponse(MyMappedThrowingException exception) {
            throw new MappableContainerException(new IOException());
        }
    }

    @Path("checked/mymappedthrowingexception")
    @GET
    @Produces("text/plain")
    public String checkedMyMappedThrowingException() throws MyMappedThrowingException {
        if (true) throw new MyMappedThrowingException();
        return "Hi there!";
    }

    @Path("webapplicationexception/{status}")
    @GET
    @Produces("text/plain")
    public String webApplicationException(
            @PathParam("status") int status,
            @QueryParam("r") String r) {
        if (true) {
            Response resp = (r == null)
                ? Response.status(status).build()
                : Response.status(status).entity(r).build();

            throw new WebApplicationException(resp);
        }
        return "Hi there!";
    }
}
