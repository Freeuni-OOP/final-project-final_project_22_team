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
        <div class="stat-number">0</div>
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

<%@ include file="footer.jsp" %>