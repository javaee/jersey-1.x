<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib prefix="rbt" uri="urn:com:sun:jersey:api:view" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Book</title>
    </head>
    <body>
        
        <h1>${it.title}</h1>
        
        Book from ${it.author}
        
        <rbt:include page="footer.jsp"/>    
        
    </body>
</html>
