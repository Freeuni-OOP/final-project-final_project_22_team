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
    <div class="logo">HikeBuddy</div>
    <%
        String uri = request.getRequestURI();
    %>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/home" class="<%= uri.contains("/home") ? "active" : "" %>">Home</a>
        <a href="${pageContext.request.contextPath}/explore" class="<%= uri.contains("/explore") ? "active" : "" %>">Explore</a>
        <a href="${pageContext.request.contextPath}/journey" class="<%= uri.contains("/journey") ? "active" : "" %>">My Journey</a>
        <a href="${pageContext.request.contextPath}/storyboard" class="<%= uri.contains("/storyboard") ? "active" : "" %>">Storyboard</a>
        <a href="${pageContext.request.contextPath}/friends" class="<%= uri.contains("/friends") ? "active" : "" %>">Friends</a>
        <a href="${pageContext.request.contextPath}/profile" class="<%= uri.contains("/profile") ? "active" : "" %>">Profile</a>
        <a href="${pageContext.request.contextPath}/home">Home</a>
        <a href="${pageContext.request.contextPath}/explore">Explore</a>
        <a href="${pageContext.request.contextPath}/journey">My Journey</a>
        <a href="${pageContext.request.contextPath}/storyboard">Storyboard</a>
        <a href="${pageContext.request.contextPath}/friends">Friends</a>
        <a href="${pageContext.request.contextPath}/profile">Profile</a>

        <%-- Notification bell (task 8.6) --%>
        <a href="${pageContext.request.contextPath}/notifications"
           style="position:relative; text-decoration:none;">
            🔔
            <%
                Integer unreadCount = (Integer) request.getAttribute("unreadCount");
                if (unreadCount != null && unreadCount > 0) {
            %>
            <span style="position:absolute; top:-8px; right:-10px;
                         background:#e53e3e; color:white; border-radius:50%;
                         font-size:11px; font-weight:bold;
                         width:18px; height:18px;
                         display:flex; align-items:center; justify-content:center;
                         line-height:1;">
                <%= unreadCount %>
            </span>
            <% } %>
        </a>

        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>

<div class="container">