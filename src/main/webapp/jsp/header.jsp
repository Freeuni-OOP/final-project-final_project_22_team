<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HikeBuddy</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<nav>
    <div class="logo">
        <svg class="logo-icon" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <circle cx="16" cy="16" r="14" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="16" cy="16" r="8" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="16" cy="16" r="2.5" fill="currentColor"/>
        </svg>
        HikeBuddy
    </div>
    <%
        String uri = request.getRequestURI();
    %>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/explore" class="<%= uri.contains("/explore") ? "active" : "" %>">Explore</a>
        <a href="${pageContext.request.contextPath}/journey" class="<%= uri.contains("/journey") ? "active" : "" %>">My Journey</a>
        <a href="${pageContext.request.contextPath}/storyboard" class="<%= uri.contains("/storyboard") ? "active" : "" %>">Storyboard</a>
        <a href="${pageContext.request.contextPath}/friends" class="<%= uri.contains("/friends") ? "active" : "" %>">Friends</a>
        <a href="${pageContext.request.contextPath}/profile" class="<%= uri.contains("/profile") ? "active" : "" %>">Profile</a>

        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>

<div class="container">