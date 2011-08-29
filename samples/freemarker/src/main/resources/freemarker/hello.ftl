<html>
<head>
  <title>Welcome!</title>
</head>
<body>
  <h1>Welcome ${user}!</h1>
  <p>items:<br />
        <#list items as item>
            ${item}<br />
        </#list>
  </p>
</body>
</html>