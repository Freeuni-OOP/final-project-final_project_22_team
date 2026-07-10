<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.hikebuddy.model.User" %>
<%@ page import="com.hikebuddy.model.FriendRequest" %>
<%@ include file="header.jsp" %>

<h1>Friends</h1>

<!-- SECTION 1: Search -->
<section class="friends-section">
    <h2>Find Friends</h2>
    <form method="get" action="${pageContext.request.contextPath}/friends">
        <input type="hidden" name="action" value="search">
        <input type="text" name="q" placeholder="Search by username" required
               value="${param.q != null ? param.q : ''}">
        <button type="submit">Search</button>
    </form>

    <%
        List<User> searchResults = (List<User>) request.getAttribute("searchResults");
        if (searchResults != null) {
            if (searchResults.isEmpty()) {
    %>
    <p>No users found.</p>
    <%
    } else {
        for (User u : searchResults) {
    %>
    <div class="friend-row">
        <span><%= u.getUsername() %></span>
        <form method="post" action="${pageContext.request.contextPath}/friends" style="display:inline;">
            <input type="hidden" name="action" value="send">
            <input type="hidden" name="targetUserId" value="<%= u.getId() %>">
            <button type="submit">Send request</button>
        </form>
    </div>
    <%
                }
            }
        }
    %>
</section>

<!-- SECTION 2: Incoming requests -->
<section class="friends-section">
    <h2>Incoming Requests</h2>
    <%
        List<FriendRequest> incoming = (List<FriendRequest>) request.getAttribute("incomingRequests");
        if (incoming == null || incoming.isEmpty()) {
    %>
    <p>None yet.</p>
    <%
    } else {
        for (FriendRequest r : incoming) {
    %>
    <div class="friend-row">
        <span><%= r.getSenderUsername() %></span>
        <form method="post" action="${pageContext.request.contextPath}/friends" style="display:inline;">
            <input type="hidden" name="action" value="accept">
            <input type="hidden" name="requestId" value="<%= r.getId() %>">
            <input type="hidden" name="senderId" value="<%= r.getSenderId() %>">
            <button type="submit">Accept</button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/friends" style="display:inline;">
            <input type="hidden" name="action" value="decline">
            <input type="hidden" name="requestId" value="<%= r.getId() %>">
            <button type="submit">Decline</button>
        </form>
    </div>
    <%
            }
        }
    %>
</section>

<!-- SECTION 3: My friends -->
<section class="friends-section">
    <h2>My Friends</h2>
    <%
        List<User> friends = (List<User>) request.getAttribute("friends");
        if (friends == null || friends.isEmpty()) {
    %>
    <p>None yet.</p>
    <%
    } else {
        for (User f : friends) {
    %>
    <div class="friend-row">
        <span><%= f.getUsername() %></span>
        <form method="post" action="${pageContext.request.contextPath}/friends" style="display:inline;">
            <input type="hidden" name="action" value="remove">
            <input type="hidden" name="friendId" value="<%= f.getId() %>">
            <button type="submit">Remove</button>
        </form>
    </div>
    <%
            }
        }
    %>
</section>

<%@ include file="footer.jsp" %>