<%@ page import="java.util.List" %>
<%@ page import="com.hikebuddy.model.User" %>
<%@ page import="com.hikebuddy.model.HikeRoute" %>
<%@ page import="com.hikebuddy.model.JourneyEntry" %>
<%@ page import="com.hikebuddy.model.StoryFolder" %>
<%@ include file="header.jsp" %>

<%!
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    User user = (User) request.getAttribute("user");
    List<HikeRoute> suggestedHikes = (List<HikeRoute>) request.getAttribute("suggestedHikes");
    List<JourneyEntry> recentJourneys = (List<JourneyEntry>) request.getAttribute("recentJourneys");
    List<StoryFolder> recentFolders = (List<StoryFolder>) request.getAttribute("recentFolders");
%>

<h1>Welcome back, <%= user.getUsername() %>!</h1>

<!-- SECTION 1: Profile summary -->
<section class="home-section">
    <h2>Your Profile</h2>
    <div class="profile-summary">
        <p><strong>Hiking level:</strong> <%= user.getHikingLevel() %></p>
        <div class="progress-bar">
            <div class="progress-fill" style="width: ${user.hikingLevel == 'INTERMEDIATE' ? '66%' : user.hikingLevel == 'ADVANCED' ? '100%' : '33%'};"></div>
        </div>
        <a href="${pageContext.request.contextPath}/profile">View full profile</a>
    </div>
</section>

<!-- SECTION 2: Suggested hikes -->
<section class="home-section">
    <h2>Suggested Hikes For You</h2>
    <%
        if (suggestedHikes == null || suggestedHikes.isEmpty()) {
    %>
    <p>No suggestions available right now.</p>
    <%
    } else {
        for (HikeRoute route : suggestedHikes) {
    %>
    <div class="hike-card">
        <h3><%= route.getName() %></h3>
        <p><%= route.getRegion() %> · <%= route.getDifficulty() %> · <%= route.getDistance() %> km</p>
        <form method="post" action="${pageContext.request.contextPath}/explore" style="display:inline;">
            <input type="hidden" name="action" value="wishlist">
            <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
            <button type="submit">Add to wishlist</button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/explore" style="display:inline;">
            <input type="hidden" name="action" value="plan">
            <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
            <button type="submit">Plan it</button>
        </form>
    </div>
    <%
            }
        }
    %>
    <a href="${pageContext.request.contextPath}/explore">See more on Explore</a>
</section>

<!-- SECTION 3: Recent journeys -->
<section class="home-section">
    <h2>Your Recent Hikes</h2>
    <%
        if (recentJourneys == null || recentJourneys.isEmpty()) {
    %>
    <p>You haven't completed any hikes yet.</p>
    <%
    } else {
        for (JourneyEntry entry : recentJourneys) {
    %>
    <div class="journey-row">
        <span><%= entry.getRouteName() %></span>
        <span><%= entry.getDate() %></span>
    </div>
    <%
            }
        }
    %>
    <a href="${pageContext.request.contextPath}/journey">View full journey</a>
</section>

<!-- SECTION 4: Recent storyboard folders -->
<section class="home-section">
    <h2>Recent Storyboard Folders</h2>
    <%
        if (recentFolders == null || recentFolders.isEmpty()) {
    %>
    <p>No folders yet.</p>
    <%
    } else {
        for (StoryFolder folder : recentFolders) {
    %>
    <a href="${pageContext.request.contextPath}/folder?folderId=<%= folder.getId() %>" class="folder-card">
        <%
            if (folder.getThumbnailPath() != null) {
        %>
        <img src="<%= folder.getThumbnailPath() %>" alt="<%= escapeHtml(folder.getName()) %>">
        <%
        } else {
        %>
        <div class="folder-placeholder">No photo</div>
        <%
            }
        %>
        <p><%= escapeHtml(folder.getName()) %></p>
    </a>
    <%
            }
        }
    %>
    <a href="${pageContext.request.contextPath}/storyboard">View all folders</a>
</section>

<%@ include file="footer.jsp" %>