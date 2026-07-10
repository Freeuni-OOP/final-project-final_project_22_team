<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<%-- New journey entry form (5.5) --%>
<div class="section-card">
    <h2>Log a Hike</h2>
    <%
        String formError = (String) request.getAttribute("formError");
        if (formError != null) {
    %>
    <div style="background:#fdecea; color:#611a15; border:1px solid #f5c6c2; padding:12px 16px; border-radius:6px; margin-bottom:16px;">
        <%= formError %>
    </div>
    <%
        }
        java.util.List<com.hikebuddy.model.HikeRoute> hikeRoutes =
                (java.util.List<com.hikebuddy.model.HikeRoute>) request.getAttribute("hikeRoutes");
    %>
    <form method="post" action="${pageContext.request.contextPath}/journey">
        <input type="hidden" name="action" value="add">

        <div class="form-group">
            <label for="hikeRouteId">Hike route</label>
            <select id="hikeRouteId" name="hikeRouteId" required>
                <option value="" disabled selected>Select a route...</option>
                <%
                    if (hikeRoutes != null) {
                        for (com.hikebuddy.model.HikeRoute route : hikeRoutes) {
                %>
                <option value="<%= route.getId() %>">
                    <%= route.getName() %> (<%= route.getDifficulty() %>)
                </option>
                <%
                        }
                    }
                %>
            </select>
        </div>

        <div class="form-group">
            <label for="date">Date</label>
            <input type="date" id="date" name="date" required>
        </div>

        <div class="form-group">
            <label for="distance">Distance (km)</label>
            <input type="number" id="distance" name="distance" step="0.1" min="0" required>
        </div>

        <div class="form-group">
            <label for="difficulty">Difficulty</label>
            <select id="difficulty" name="difficulty" required>
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
            </select>
        </div>

        <div class="form-group">
            <label for="notes">Notes</label>
            <textarea id="notes" name="notes" placeholder="Optional notes..."></textarea>
        </div>

        <div class="form-group">
            <label>Status</label>
            <div style="display:flex; margin-top:6px;">
                <label style="font-weight:normal; display:flex; align-items:center; margin-right:20px;">
                    <input type="radio" name="status" value="PENDING" checked style="width:auto; margin-right:6px;"> Planned
                </label>
                <label style="font-weight:normal; display:flex; align-items:center;">
                    <input type="radio" name="status" value="WISHLIST" style="width:auto; margin-right:6px;"> Wishlist
                </label>
            </div>
        </div>

        <button type="submit" class="btn-green">Log hike</button>
    </form>
</div>

<%-- Journey Entry section (5.3): Planned & Completed --%>
<div class="section-card">
    <h2>Planned & Completed Hikes</h2>
    <%
        java.util.List<com.hikebuddy.model.JourneyEntry> plannedEntries =
                (java.util.List<com.hikebuddy.model.JourneyEntry>) request.getAttribute("plannedEntries");
        if (plannedEntries != null && !plannedEntries.isEmpty()) {
            for (com.hikebuddy.model.JourneyEntry entry : plannedEntries) {
                String status = entry.getStatus();
                String statusColor = "PENDING".equals(status) ? "#b8860b" : "#2e7d32";
                String statusBg = "PENDING".equals(status) ? "#fff8e1" : "#e8f5e9";
    %>
    <div class="entry-row">
        <div>
            <strong><%= entry.getRouteName() != null ? entry.getRouteName() : "Unnamed hike" %></strong>
            <div style="font-size:0.85em; color:#666;">
                <%= entry.getDate() %> &middot; <%= entry.getDifficulty() %>
            </div>
        </div>
        <div style="display:flex; align-items:center; gap:10px;">
            <span style="background:<%= statusBg %>; color:<%= statusColor %>; padding:3px 10px; border-radius:12px; font-size:0.8em; font-weight:bold;">
                <%= status %>
            </span>
            <% if ("PENDING".equals(status)) { %>
            <form method="post" action="${pageContext.request.contextPath}/journey" style="display:inline;">
                <input type="hidden" name="action" value="status">
                <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                <input type="hidden" name="newStatus" value="COMPLETED">
                <button type="submit" class="btn-green">Mark completed</button>
            </form>
            <% } %>
            <form method="post" action="${pageContext.request.contextPath}/journey" style="display:inline;">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                <button type="submit" class="btn-red">Delete</button>
            </form>
        </div>
    </div>
    <%
            }
        } else {
    %>
    <p style="color:#aaa;">No hikes planned yet.</p>
    <%
        }
    %>
</div>

<%-- Journey Entry section (5.4): Wishlist --%>
<div class="section-card">
    <h2>Wishlist</h2>
    <p style="font-size:0.85em; color:#888;">You can add hikes to your wishlist from the Explore page.</p>
    <%
        java.util.List<com.hikebuddy.model.JourneyEntry> wishlistEntries =
                (java.util.List<com.hikebuddy.model.JourneyEntry>) request.getAttribute("wishlistEntries");
        if (wishlistEntries != null && !wishlistEntries.isEmpty()) {
            for (com.hikebuddy.model.JourneyEntry entry : wishlistEntries) {
    %>
    <div class="entry-row">
        <div>
            <strong><%= entry.getRouteName() != null ? entry.getRouteName() : "Unnamed hike" %></strong>
            <div style="font-size:0.85em; color:#666;"><%= entry.getDifficulty() %></div>
        </div>
        <div style="display:flex; align-items:center; gap:10px;">
            <form method="post" action="${pageContext.request.contextPath}/journey" style="display:inline;">
                <input type="hidden" name="action" value="status">
                <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                <input type="hidden" name="newStatus" value="PENDING">
                <button type="submit" class="btn-green">Move to planned</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/journey" style="display:inline;">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                <button type="submit" class="btn-red">Remove</button>
            </form>
        </div>
    </div>
    <%
            }
        } else {
    %>
    <p style="color:#aaa;">No hikes in wishlist yet.</p>
    <%
        }
    %>
</div>

<%@ include file="footer.jsp" %>