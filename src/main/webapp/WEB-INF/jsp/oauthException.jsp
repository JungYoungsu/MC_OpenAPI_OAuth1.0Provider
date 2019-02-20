<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" isErrorPage="true"%>
<%@ page import="java.util.*" %>    
<%
	Exception ex = (Exception)request.getAttribute("exception");
%>    
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Oh My Godness!!</title>
</head>
<body>
  OAuthException : <%= ex.getMessage() %>
</body>
</html>