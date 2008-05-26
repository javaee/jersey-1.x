<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib prefix="rbt" uri="urn:com:sun:jersey:api:view" %> 

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CD</title>
    </head>
    <body>

    <h1>${it.title}</h1>
    
    <p>CD from ${it.author}
    
    <h2>Track List</h2>

    <ul>
        <c:forEach var="t" items="${it.tracks}" varStatus="loop">
            <li><a href="tracks/${loop.index}">${t.name}</a>
        </c:forEach>
    </ul>
    
    <rbt:include page="footer.jsp"/> 
    
    </body>
</html>
