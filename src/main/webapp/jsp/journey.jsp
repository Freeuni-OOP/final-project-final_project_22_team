<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<%!
    private String formatDate(java.sql.Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy");
        return sdf.format(date);
    }
%>

<h1>My Journey</h1>

<%
    java.util.List<com.hikebuddy.model.JourneyEntry> plannedEntries =
            (java.util.List<com.hikebuddy.model.JourneyEntry>) request.getAttribute("plannedEntries");
    if (plannedEntries == null) plannedEntries = new java.util.ArrayList<>();
    java.util.List<com.hikebuddy.model.JourneyEntry> wishlistEntries =
            (java.util.List<com.hikebuddy.model.JourneyEntry>) request.getAttribute("wishlistEntries");
    if (wishlistEntries == null) wishlistEntries = new java.util.ArrayList<>();
    String formError = (String) request.getAttribute("formError");
    java.util.List<com.hikebuddy.model.HikeRoute> hikeRoutes =
            (java.util.List<com.hikebuddy.model.HikeRoute>) request.getAttribute("hikeRoutes");
%>

<div class="journey-tabs">
    <button type="button" class="journey-tab active" data-tab="planned">
        Planned &amp; Completed <span class="tab-count"><%= plannedEntries.size() %></span>
    </button>
    <button type="button" class="journey-tab" data-tab="wishlist">
        Wishlist <span class="tab-count"><%= wishlistEntries.size() %></span>
    </button>
</div>

<div class="journey-layout">
    <div class="journey-list-col">

        <%-- Journey Entry section (5.3): Planned & Completed --%>
        <div id="tab-planned" class="journey-tab-panel">
            <%
                if (plannedEntries.isEmpty()) {
            %>
            <p style="color:var(--color-text-muted);">No hikes planned yet.</p>
            <%
                } else {
                    for (com.hikebuddy.model.JourneyEntry entry : plannedEntries) {
                        String status = entry.getStatus();
                        boolean isPending = "PENDING".equals(status);
            %>
            <div class="journey-card">
                <div class="journey-card-thumb"></div>
                <div class="journey-card-body">
                    <div class="journey-card-title-row">
                        <h3><%= entry.getRouteName() != null ? entry.getRouteName() : "Unnamed hike" %></h3>
                        <span class="journey-status-badge <%= isPending ? "pending" : "completed" %>">
                            <%= isPending ? "Pending" : "Completed" %>
                        </span>
                    </div>
                    <div class="journey-card-stats">
                        <span>&#128197;</span>
                        <span><%= isPending ? "Planned &middot; " + formatDate(entry.getDate()) : formatDate(entry.getDate()) %></span>
                        <span>&middot;</span>
                        <span><%= entry.getDistance() %> km</span>
                        <span>&middot;</span>
                        <span><%= entry.getDifficulty() %></span>
                    </div>
                </div>
                <div class="journey-card-actions">
                    <form method="post" action="${pageContext.request.contextPath}/journey" style="margin:0;">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                        <button type="submit" class="journey-card-remove" aria-label="Remove">&times;</button>
                    </form>
                    <% if (isPending) { %>
                    <form method="post" action="${pageContext.request.contextPath}/journey" style="margin:0;">
                        <input type="hidden" name="action" value="status">
                        <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                        <input type="hidden" name="newStatus" value="COMPLETED">
                        <button type="submit" class="journey-card-link-btn">Mark completed</button>
                    </form>
                    <% } %>
                </div>
            </div>
            <%
                    }
                }
            %>
        </div>

        <%-- Journey Entry section (5.4): Wishlist --%>
        <div id="tab-wishlist" class="journey-tab-panel" style="display:none;">
            <%
                if (wishlistEntries.isEmpty()) {
            %>
            <p style="color:var(--color-text-muted);">No hikes in wishlist yet. You can add hikes to your wishlist from the Explore page.</p>
            <%
                } else {
                    for (com.hikebuddy.model.JourneyEntry entry : wishlistEntries) {
            %>
            <div class="journey-card">
                <div class="journey-card-thumb"></div>
                <div class="journey-card-body">
                    <div class="journey-card-title-row">
                        <h3><%= entry.getRouteName() != null ? entry.getRouteName() : "Unnamed hike" %></h3>
                    </div>
                    <div class="journey-card-stats">
                        <span><%= entry.getDifficulty() %></span>
                    </div>
                </div>
                <div class="journey-card-actions">
                    <form method="post" action="${pageContext.request.contextPath}/journey" style="margin:0;">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                        <button type="submit" class="journey-card-remove" aria-label="Remove">&times;</button>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/journey" style="margin:0;">
                        <input type="hidden" name="action" value="status">
                        <input type="hidden" name="entryId" value="<%= entry.getId() %>">
                        <input type="hidden" name="newStatus" value="PENDING">
                        <button type="submit" class="journey-card-link-btn">Move to planned</button>
                    </form>
                </div>
            </div>
            <%
                    }
                }
            %>
        </div>

    </div>

    <div class="journey-form-col">
        <div class="section-card">
            <h2>Log a new hike</h2>
            <p style="color:var(--color-text-muted); font-size:14px; margin-bottom:20px;">
                Adds it here and auto-creates a storyboard folder.
            </p>
            <%
                if (formError != null) {
            %>
            <div style="background:#fdecea; color:#611a15; border:1px solid #f5c6c2; padding:12px 16px; border-radius:6px; margin-bottom:16px;">
                <%= formError %>
            </div>
            <%
                }
            %>
            <form method="post" action="${pageContext.request.contextPath}/journey">
                <input type="hidden" name="action" value="add">

                <div class="form-group">
                    <label for="hikeRouteId">Trail</label>
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

                <div class="form-row">
                    <div class="form-group">
                        <label for="date">Date</label>
                        <input type="date" id="date" name="date" required>
                    </div>
                    <div class="form-group">
                        <label for="distance">Distance (km)</label>
                        <input type="number" id="distance" name="distance" step="0.1" min="0" required>
                    </div>
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
                        <label style="font-family:var(--font-sans); text-transform:none; letter-spacing:normal; font-weight:normal; display:flex; align-items:center; margin-right:20px;">
                            <input type="radio" name="status" value="PENDING" checked style="width:auto; margin-right:6px;"> Planned
                        </label>
                        <label style="font-family:var(--font-sans); text-transform:none; letter-spacing:normal; font-weight:normal; display:flex; align-items:center;">
                            <input type="radio" name="status" value="WISHLIST" style="width:auto; margin-right:6px;"> Wishlist
                        </label>
                    </div>
                </div>

                <button type="submit" class="btn-green" style="width:100%;">Add to journey</button>
            </form>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var tabs = document.querySelectorAll('.journey-tab');
    var panels = document.querySelectorAll('.journey-tab-panel');
    tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
            tabs.forEach(function (t) { t.classList.remove('active'); });
            panels.forEach(function (p) { p.style.display = 'none'; });
            tab.classList.add('active');
            document.getElementById('tab-' + tab.getAttribute('data-tab')).style.display = '';
        });
    });
});
</script>

<%@ include file="footer.jsp" %>
