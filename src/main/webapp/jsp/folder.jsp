<%@ include file="header.jsp" %>

<%
    com.hikebuddy.model.StoryFolder folder =
            (com.hikebuddy.model.StoryFolder) request.getAttribute("folder");
    java.util.List<com.hikebuddy.model.Photo> photos =
            (java.util.List<com.hikebuddy.model.Photo>) request.getAttribute("photos");
%>

<a href="${pageContext.request.contextPath}/storyboard" style="color:#2d6a4f; text-decoration:none;">
    &larr; Back to Storyboard
</a>

<h1 style="margin-top:16px;"><%= folder.getName() %></h1>

<%-- Upload form --%>
<div class="section-card">
    <h2>Upload a photo</h2>
    <form method="post" action="${pageContext.request.contextPath}/folder?folderId=<%= folder.getId() %>"
          enctype="multipart/form-data" style="display:flex; gap:10px; align-items:center;">
        <input type="hidden" name="action" value="upload">
        <input type="file" name="photo" accept="image/*" required>
        <button type="submit" class="btn-green">Upload photo</button>
    </form>
</div>

<%-- Photo grid --%>
<% if (photos != null && !photos.isEmpty()) { %>
<div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(200px, 1fr)); gap:16px;">
    <%
        for (com.hikebuddy.model.Photo photo : photos) {
    %>
    <div style="position:relative;">
        <img src="${pageContext.request.contextPath}<%= photo.getFilePath() %>"
             style="width:100%; height:200px; object-fit:cover; border-radius:8px; display:block;">
        <form method="post" action="${pageContext.request.contextPath}/folder?folderId=<%= folder.getId() %>"
              style="margin-top:8px;">
            <input type="hidden" name="action" value="deletePhoto">
            <input type="hidden" name="photoId" value="<%= photo.getId() %>">
            <button type="submit" class="btn-red"
                    style="width:100%; background-color:#c0392b; color:white; border:none;
                               padding:6px; border-radius:6px; cursor:pointer; font-size:0.85em;">
                Delete
            </button>
        </form>
    </div>
    <%
        }
    %>
</div>
<% } else { %>
<p style="color:#aaa; margin-top:20px;">No photos yet. Upload your first photo above.</p>
<% } %>

<%@ include file="footer.jsp" %>