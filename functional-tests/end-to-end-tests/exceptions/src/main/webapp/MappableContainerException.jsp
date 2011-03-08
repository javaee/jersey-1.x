<%-- 
    Document   : SecurityExceptionErrorPage
    Created on : Oct 15, 2009, 11:39:43 AM
    Author     : paulsandoz
--%>

<%@page isErrorPage="true" contentType="text/html" pageEncoding="MacRoman"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=MacRoman">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>MappableContainerException error page</h1>
        <h1>Exception: <%= exception.getClass().getName() %></h1>
        <h1>Cause: <%= exception.getCause().getClass().getName() %></h1>
    </body>
</html>
