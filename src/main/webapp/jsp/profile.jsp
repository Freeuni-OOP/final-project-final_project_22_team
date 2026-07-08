<%@ include file="header.jsp" %>

<%
    com.hikebuddy.model.User user = (com.hikebuddy.model.User) request.getAttribute("user");
    String hikingLevel = user.getHikingLevel();

    // Calculate progress bar width based on hiking level
    String progressWidth = "33%";
    if ("INTERMEDIATE".equals(hikingLevel)) progressWidth = "66%";
    else if ("ADVANCED".equals(hikingLevel)) progressWidth = "100%";
%>

<div class="profile-header">
    <h1><%= user.getUsername() %>'s Profile</h1>
    <p>Member since <%= user.getCreatedAt() != null ? user.getCreatedAt().toString().substring(0, 10) : "recently" %></p>
</div>

<%-- Success message after profile update --%>
<% if ("updated".equals(request.getParameter("success"))) { %>
<div class="success-msg">Profile updated successfully.</div>
<% } %>

<%-- Stats row --%>
<div class="stats-row">
    <div class="stat-card">
        <div class="stat-number">0</div>
        <div class="stat-label">Hikes</div>
    </div>
    <div class="stat-card">
        <div class="stat-number">0</div>
        <div class="stat-label">Gear items</div>
    </div>
    <div class="stat-card">
        <div class="stat-number">${friendCount}</div>
        <div class="stat-label">Friends</div>
    </div>
    <div class="level-card">
        <div class="stat-label">Hiking level</div>
        <div class="stat-number"><%= hikingLevel %></div>
        <div class="progress-bar-bg">
            <div class="progress-bar-fill" style="width: <%= progressWidth %>;"></div>
        </div>
    </div>
</div>

<%-- Bio section --%>
<div class="section-card">
    <h2>About me</h2>
    <% if (user.getBio() != null && !user.getBio().isEmpty()) { %>
    <p class="bio-text"><%= user.getBio() %></p>
    <% } else { %>
    <p class="bio-text" style="color: #aaa;">No bio yet. Edit your profile to add one.</p>
    <% } %>
</div>

<%-- Edit profile form --%>
<div class="section-card">
    <h2>Edit profile</h2>
    <form method="post" action="${pageContext.request.contextPath}/profile">

        <div class="form-group">
            <label>Bio</label>
            <textarea name="bio"><%= user.getBio() != null ? user.getBio() : "" %></textarea>
        </div>

        <div class="form-group">
            <label>Hiking level</label>
            <select name="hikingLevel">
                <option value="BEGINNER"     <%= "BEGINNER".equals(hikingLevel)     ? "selected" : "" %>>Beginner</option>
                <option value="INTERMEDIATE" <%= "INTERMEDIATE".equals(hikingLevel) ? "selected" : "" %>>Intermediate</option>
                <option value="ADVANCED"     <%= "ADVANCED".equals(hikingLevel)     ? "selected" : "" %>>Advanced</option>
            </select>
        </div>

        <button type="submit" class="btn-green">Save changes</button>
    </form>
</div>

<%-- Gear checklist section --%>
<div class="section-card">
    <h2>My Gear</h2>

    <%
        java.util.List<com.hikebuddy.model.Gear> gearList =
                (java.util.List<com.hikebuddy.model.Gear>) request.getAttribute("gearList");
        if (gearList != null && !gearList.isEmpty()) {
            for (com.hikebuddy.model.Gear gear : gearList) {
    %>
    <form method="post" action="${pageContext.request.contextPath}/gear"
          style="display:flex; align-items:center; gap:10px; margin-bottom:8px;">
        <input type="hidden" name="action" value="toggle">
        <input type="hidden" name="gearId" value="<%= gear.getId() %>">
        <input type="hidden" name="currentState" value="<%= gear.isChecked() %>">
        <input type="checkbox"
            <%= gear.isChecked() ? "checked" : "" %>
               onchange="this.form.submit()">
        <span style="<%= gear.isChecked() ? "text-decoration:line-through; color:#aaa;" : "" %>">
            <%= gear.getName() %>
        </span>
    </form>
    <%
        }
    } else {
    %>
    <p style="color:#aaa; margin-bottom:16px;">No gear yet. Add your first item below.</p>
    <%
        }
    %>

    <%-- Add new gear form --%>
    <form method="post" action="${pageContext.request.contextPath}/gear"
          style="display:flex; gap:10px; margin-top:16px;">
        <input type="hidden" name="action" value="add">
        <input type="text" name="name" placeholder="Add gear item..."
               class="form-group" style="flex:1; margin:0;">
        <button type="submit" class="btn-green">Add</button>
    </form>
</div>

<%-- Availability calendar section --%>
<div class="section-card">
    <h2>My Availability</h2>
    <p style="color:#666; margin-bottom:16px; font-size:14px;">
        Click a day to mark/unmark it as available for hiking.
    </p>

    <%
        java.util.Set<Integer> availableDays =
                (java.util.Set<Integer>) request.getAttribute("availableDays");
        java.time.LocalDate now = java.time.LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int daysInMonth = now.lengthOfMonth();
        java.time.DayOfWeek firstDayOfWeek = java.time.LocalDate.of(year, month, 1).getDayOfWeek();
        int startOffset = firstDayOfWeek.getValue() % 7;
    %>

    <p style="font-weight:500; margin-bottom:12px;">
        <%= now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) %>
        <%= year %>
    </p>

    <table style="border-collapse:collapse; width:100%;">
        <tr>
            <th style="padding:6px; text-align:center; color:#666;">Sun</th>
            <th style="padding:6px; text-align:center; color:#666;">Mon</th>
            <th style="padding:6px; text-align:center; color:#666;">Tue</th>
            <th style="padding:6px; text-align:center; color:#666;">Wed</th>
            <th style="padding:6px; text-align:center; color:#666;">Thu</th>
            <th style="padding:6px; text-align:center; color:#666;">Fri</th>
            <th style="padding:6px; text-align:center; color:#666;">Sat</th>
        </tr>
        <tr>
            <%
                int col = 0;
                for (int i = 0; i < startOffset; i++) {
            %>
            <td></td>
            <%
                    col++;
                }
                for (int day = 1; day <= daysInMonth; day++) {
                    String dateStr = String.format("%d-%02d-%02d", year, month, day);
                    boolean isAvailable = availableDays != null && availableDays.contains(day);
                    String bgColor = isAvailable ? "#2d6a4f" : "#f4f6f4";
                    String txtColor = isAvailable ? "white" : "#2b2b2b";
            %>
            <td style="padding:4px; text-align:center;">
                <form method="post" action="${pageContext.request.contextPath}/calendar"
                      style="margin:0;">
                    <input type="hidden" name="date" value="<%= dateStr %>">
                    <button type="submit"
                            style="width:36px; height:36px; border-radius:50%; border:none;
                                    background:<%= bgColor %>; color:<%= txtColor %>;
                                    cursor:pointer; font-size:13px;">
                        <%= day %>
                    </button>
                </form>
            </td>
            <%
                col++;
                if (col == 7 && day < daysInMonth) {
            %>
        </tr><tr>
        <%
                    col = 0;
                }
            }
            while (col < 7) {
        %>
        <td></td>
        <%
                col++;
            }
        %>
    </tr>
    </table>
</div>

<%-- Badges section --%>
<div class="section-card">
    <h2>Badges</h2>
    <%
        java.util.List<com.hikebuddy.model.Badge> badges =
                (java.util.List<com.hikebuddy.model.Badge>) request.getAttribute("badges");
        if (badges != null && !badges.isEmpty()) {
            for (com.hikebuddy.model.Badge badge : badges) {
    %>
    <div style="display:inline-block; background:#ecfdf5; border:1px solid #a7f3d0;
                border-radius:8px; padding:10px 16px; margin:6px; font-weight:500;
                color:#065f46;">
        <%= badge.getDisplayName() %>
    </div>
    <%
        }
    } else {
    %>
    <p style="color:#aaa;">Complete hikes and missions to earn badges!</p>
    <%
        }
    %>
</div>

<%-- Friends preview section (task 3.10) --%>
<div class="section-card">
    <h2>Friends</h2>
    <%
        java.util.List<com.hikebuddy.model.User> friendsPreview =
                (java.util.List<com.hikebuddy.model.User>) request.getAttribute("friendsPreview");
        if (friendsPreview != null && !friendsPreview.isEmpty()) {
            for (com.hikebuddy.model.User friend : friendsPreview) {
    %>
    <div class="friend-row">
        <span><%= friend.getUsername() %></span>
    </div>
    <%
        }
    } else {
    %>
    <p style="color:#aaa;">No friends yet.</p>
    <%
        }
    %>
    <div style="margin-top:16px;">
        <a href="${pageContext.request.contextPath}/friends"
           style="color:#2d6a4f; font-weight:500; text-decoration:none;">
            View all friends →
        </a>
    </div>
</div>

<%@ include file="footer.jsp" %>