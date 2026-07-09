<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<%-- Section 1: Search and Filter --%>
<div class="section-card">
    <h2>Search Hikes</h2>
    <form method="get" action="${pageContext.request.contextPath}/explore"
          style="display:flex; gap:10px; flex-wrap:wrap; align-items:flex-end;">
        <div class="form-group" style="flex:1; min-width:200px; margin:0;">
            <label for="q">Search by name or region</label>
            <input type="text" id="q" name="q"
                   placeholder="e.g. Kazbegi, Blue Ridge..."
                   value="<%= request.getAttribute("searchQuery") != null ? request.getAttribute("searchQuery") : "" %>">
        </div>
        <div class="form-group" style="margin:0;">
            <label for="diff">Difficulty</label>
            <select id="diff" name="diff">
                <option value="ALL"    <%= "ALL".equals(request.getAttribute("searchDiff"))    || request.getAttribute("searchDiff") == null ? "selected" : "" %>>All</option>
                <option value="EASY"   <%= "EASY".equals(request.getAttribute("searchDiff"))   ? "selected" : "" %>>Easy</option>
                <option value="MEDIUM" <%= "MEDIUM".equals(request.getAttribute("searchDiff")) ? "selected" : "" %>>Medium</option>
                <option value="HARD"   <%= "HARD".equals(request.getAttribute("searchDiff"))   ? "selected" : "" %>>Hard</option>
            </select>
        </div>
        <button type="submit" class="btn-green">Search</button>
        <a href="${pageContext.request.contextPath}/explore"
           style="color:#2d6a4f; text-decoration:none; padding:10px 0;">Clear</a>
    </form>

    <%-- Search results --%>
    <%
        java.util.List<com.hikebuddy.model.HikeRoute> searchResults =
                (java.util.List<com.hikebuddy.model.HikeRoute>) request.getAttribute("searchResults");
        if (searchResults != null) {
    %>
    <div style="margin-top:20px;">
        <h3 style="color:#1b4332; margin-bottom:12px;">
            Search results (<%= searchResults.size() %>)
        </h3>
        <% if (searchResults.isEmpty()) { %>
        <p style="color:#aaa;">No hikes found matching your search.</p>
        <% } else {
            for (com.hikebuddy.model.HikeRoute route : searchResults) { %>
        <div style="border:1px solid #dce8dc; border-radius:8px; padding:16px;
                    margin-bottom:10px; background:white; display:flex;
                    justify-content:space-between; align-items:center;
                    flex-wrap:wrap; gap:10px;">
            <div>
                <strong style="font-size:15px;"><%= route.getName() %></strong>
                <span style="background:#e0f2f1; color:#00695c; border-radius:4px;
                             padding:2px 8px; font-size:12px; margin-left:8px;">
                    <%= route.getDifficulty() %>
                </span>
                <div style="color:#666; font-size:13px; margin-top:4px;">
                    <%= route.getRegion() %> &nbsp;·&nbsp; <%= route.getDistance() %> km
                </div>
            </div>
            <div style="display:flex; gap:8px;">
                <form method="post" action="${pageContext.request.contextPath}/explore" style="margin:0;">
                    <input type="hidden" name="action" value="wishlist">
                    <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
                    <input type="hidden" name="difficulty" value="<%= route.getDifficulty() %>">
                    <input type="hidden" name="routeName" value="<%= route.getName() %>">
                    <button type="submit"
                            style="background:#f0fdf4; color:#2d6a4f; border:1px solid #bbf7d0;
                                   border-radius:6px; padding:6px 12px; font-size:13px; cursor:pointer;">
                        + Wishlist
                    </button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/explore" style="margin:0;">
                    <input type="hidden" name="action" value="plan">
                    <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
                    <input type="hidden" name="difficulty" value="<%= route.getDifficulty() %>">
                    <input type="hidden" name="routeName" value="<%= route.getName() %>">
                    <button type="submit" class="btn-green" style="font-size:13px; padding:6px 12px;">
                        Plan it
                    </button>
                </form>
            </div>
        </div>
        <%  }
        } %>
    </div>
    <% } %>
</div>

<%-- Section 2: Suggested Hikes --%>
<div class="section-card">
    <h2>Suggested for You</h2>
    <p style="color:#666; font-size:14px; margin-bottom:16px;">
        Based on your hiking level.
    </p>
    <%
        java.util.List<com.hikebuddy.model.HikeRoute> suggestedHikes =
                (java.util.List<com.hikebuddy.model.HikeRoute>) request.getAttribute("suggestedHikes");
        if (suggestedHikes == null || suggestedHikes.isEmpty()) {
    %>
    <p style="color:#aaa;">
        No suggestions yet — log some hikes to get personalized suggestions!
    </p>
    <%  } else { %>
    <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(280px,1fr)); gap:16px;">
        <%
            for (com.hikebuddy.model.HikeRoute route : suggestedHikes) {
                String diffColor = "EASY".equals(route.getDifficulty()) ? "#065f46" :
                        "MEDIUM".equals(route.getDifficulty()) ? "#92400e" : "#7f1d1d";
                String diffBg    = "EASY".equals(route.getDifficulty()) ? "#ecfdf5" :
                        "MEDIUM".equals(route.getDifficulty()) ? "#fffbeb" : "#fef2f2";
        %>
        <div style="border:1px solid #dce8dc; border-radius:8px; padding:16px;
                    background:white; display:flex; flex-direction:column; gap:10px;">
            <div>
                <div style="display:flex; align-items:center; gap:8px; margin-bottom:6px;">
                    <strong><%= route.getName() %></strong>
                    <span style="background:<%= diffBg %>; color:<%= diffColor %>;
                            border-radius:4px; padding:2px 8px; font-size:12px;">
                        <%= route.getDifficulty() %>
                    </span>
                </div>
                <div style="color:#666; font-size:13px;">
                    <%= route.getRegion() %> &nbsp;·&nbsp; <%= route.getDistance() %> km
                </div>
                <% if (route.getDescription() != null && !route.getDescription().isEmpty()) { %>
                <div style="color:#444; font-size:13px; margin-top:6px; line-height:1.4;">
                    <%= route.getDescription().length() > 80
                            ? route.getDescription().substring(0, 80) + "..."
                            : route.getDescription() %>
                </div>
                <% } %>
            </div>
            <div style="display:flex; gap:8px; margin-top:auto;">
                <form method="post" action="${pageContext.request.contextPath}/explore"
                      style="margin:0; flex:1;">
                    <input type="hidden" name="action" value="wishlist">
                    <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
                    <input type="hidden" name="difficulty" value="<%= route.getDifficulty() %>">
                    <input type="hidden" name="routeName" value="<%= route.getName() %>">
                    <button type="submit"
                            style="width:100%; background:#f0fdf4; color:#2d6a4f;
                                   border:1px solid #bbf7d0; border-radius:6px;
                                   padding:7px; font-size:13px; cursor:pointer;">
                        + Wishlist
                    </button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/explore"
                      style="margin:0; flex:1;">
                    <input type="hidden" name="action" value="plan">
                    <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
                    <input type="hidden" name="difficulty" value="<%= route.getDifficulty() %>">
                    <input type="hidden" name="routeName" value="<%= route.getName() %>">
                    <button type="submit" class="btn-green"
                            style="width:100%; font-size:13px; padding:7px;">
                        Plan it
                    </button>
                </form>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>
</div>

<%-- Section 3: Your Recent Hikes --%>
<div class="section-card">
    <h2>Your Recent Hikes</h2>
    <%
        java.util.List<com.hikebuddy.model.JourneyEntry> recentHikes =
                (java.util.List<com.hikebuddy.model.JourneyEntry>) request.getAttribute("recentHikes");
        if (recentHikes == null || recentHikes.isEmpty()) {
    %>
    <p style="color:#aaa;">
        No completed hikes yet. Log your first hike on the
        <a href="${pageContext.request.contextPath}/journey"
           style="color:#2d6a4f;">Journey page</a>!
    </p>
    <%  } else {
        for (com.hikebuddy.model.JourneyEntry entry : recentHikes) {
    %>
    <div style="display:flex; align-items:center; justify-content:space-between;
                padding:12px 0; border-bottom:1px solid #eee;">
        <div>
            <strong>
                <%= entry.getRouteName() != null ? entry.getRouteName() : "Unknown route" %>
            </strong>
            <div style="color:#666; font-size:13px; margin-top:2px;">
                <%= entry.getDate() %> &nbsp;·&nbsp; <%= entry.getDistance() %> km
            </div>
        </div>
        <span style="background:#ecfdf5; color:#065f46; border-radius:4px;
                     padding:2px 8px; font-size:12px; font-weight:500;">
            <%= entry.getDifficulty() %>
        </span>
    </div>
    <%      }
    }
    %>
</div>

<%-- Section 4: Friends' Recent Hikes --%>
<div class="section-card">
    <h2>Friends' Recent Hikes</h2>
    <%
        java.util.List<com.hikebuddy.model.JourneyEntry> friendHikes =
                (java.util.List<com.hikebuddy.model.JourneyEntry>) request.getAttribute("friendHikes");
        if (friendHikes == null || friendHikes.isEmpty()) {
    %>
    <p style="color:#aaa;">
        No friend activity yet.
        <a href="${pageContext.request.contextPath}/friends"
           style="color:#2d6a4f;">Add some friends</a>
        to see what they've been hiking!
    </p>
    <%  } else {
        for (com.hikebuddy.model.JourneyEntry entry : friendHikes) {
    %>
    <div style="display:flex; align-items:center; justify-content:space-between;
                padding:12px 0; border-bottom:1px solid #eee;">
        <div>
            <strong>
                <%= entry.getRouteName() != null ? entry.getRouteName() : "Unknown route" %>
            </strong>
            <div style="color:#666; font-size:13px; margin-top:2px;">
                <%= entry.getDate() %>
            </div>
        </div>
        <span style="background:#e0f2f1; color:#00695c; border-radius:4px;
                     padding:2px 8px; font-size:12px; font-weight:500;">
            <%= entry.getDifficulty() %>
        </span>
    </div>
    <%      }
    }
    %>
</div>

<%@ include file="footer.jsp" %>