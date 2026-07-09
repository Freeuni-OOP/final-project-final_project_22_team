<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>

<%
    com.hikebuddy.model.HikeRoute route =
            (com.hikebuddy.model.HikeRoute) request.getAttribute("route");

    String diffColor = "EASY".equals(route.getDifficulty()) ? "#065f46" :
            "MEDIUM".equals(route.getDifficulty()) ? "#92400e" : "#7f1d1d";
    String diffBg    = "EASY".equals(route.getDifficulty()) ? "#ecfdf5" :
            "MEDIUM".equals(route.getDifficulty()) ? "#fffbeb" : "#fef2f2";
%>

<%-- Back link --%>
<div style="margin-bottom:20px;">
    <a href="${pageContext.request.contextPath}/explore"
       style="color:#2d6a4f; text-decoration:none; font-weight:500;">
        ← Back to Explore
    </a>
</div>

<%-- Route detail card --%>
<div class="section-card">

    <%-- Header: name + difficulty badge --%>
    <div style="display:flex; align-items:center; gap:12px; margin-bottom:16px; flex-wrap:wrap;">
        <h1 style="color:#1b4332; margin:0; font-size:24px;">
            <%= route.getName() %>
        </h1>
        <span style="background:<%= diffBg %>; color:<%= diffColor %>;
                border-radius:4px; padding:4px 12px; font-size:14px; font-weight:600;">
            <%= route.getDifficulty() %>
        </span>
    </div>

    <%-- Stats row --%>
    <div style="display:flex; gap:24px; margin-bottom:20px; flex-wrap:wrap;">
        <div>
            <div style="color:#666; font-size:13px; margin-bottom:2px;">Region</div>
            <div style="font-weight:500;">
                <%= route.getRegion() != null ? route.getRegion() : "—" %>
            </div>
        </div>
        <div>
            <div style="color:#666; font-size:13px; margin-bottom:2px;">Distance</div>
            <div style="font-weight:500;"><%= route.getDistance() %> km</div>
        </div>
        <div>
            <div style="color:#666; font-size:13px; margin-bottom:2px;">Difficulty</div>
            <div style="font-weight:500;"><%= route.getDifficulty() %></div>
        </div>
    </div>

    <%-- Description --%>
    <% if (route.getDescription() != null && !route.getDescription().isEmpty()) { %>
    <div style="color:#444; font-size:15px; line-height:1.7; margin-bottom:24px;">
        <%= route.getDescription() %>
    </div>
    <% } else { %>
    <div style="color:#aaa; margin-bottom:24px;">No description available.</div>
    <% } %>

    <%-- Action buttons --%>
    <div style="display:flex; gap:12px; flex-wrap:wrap;">
        <form method="post" action="${pageContext.request.contextPath}/hike" style="margin:0;">
            <input type="hidden" name="action" value="wishlist">
            <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
            <button type="submit"
                    style="background:#f0fdf4; color:#2d6a4f; border:1px solid #bbf7d0;
                           border-radius:6px; padding:10px 20px; font-size:15px; cursor:pointer;">
                + Add to Wishlist
            </button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/hike" style="margin:0;">
            <input type="hidden" name="action" value="plan">
            <input type="hidden" name="hikeRouteId" value="<%= route.getId() %>">
            <button type="submit" class="btn-green"
                    style="padding:10px 20px; font-size:15px;">
                Plan this hike
            </button>
        </form>
    </div>
</div>

<%@ include file="footer.jsp" %>