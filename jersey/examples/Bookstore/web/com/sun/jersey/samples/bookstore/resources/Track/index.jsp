<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>CD Track</title>
    </head>
    <body>

    <h1>${it.name}</h1>
    
    <br>Track length: ${it.length} sec.
    
    <p><a href="../..">back</a>
    
    </body>
</html>
