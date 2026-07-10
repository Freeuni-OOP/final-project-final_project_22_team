<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
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

<h1>My Storyboard</h1>

<%-- Create folder form --%>
<div class="section-card">
    <h2>Create a new folder</h2>

    <% if ("emptyname".equals(request.getParameter("error"))) { %>
    <div style="background:#fdecea; color:#b91c1c; border:1px solid #f5c6c6;
                padding:10px 14px; border-radius:6px; margin-bottom:12px;">
        Folder name cannot be empty.
    </div>
    <% } %>
    <form method="post" action="${pageContext.request.contextPath}/storyboard"
          style="display:flex; gap:10px;">
        <input type="hidden" name="action" value="create">
        <input type="text" name="folderName" placeholder="Folder name..."
               required style="flex:1; padding:10px 12px; border:1px solid #ccc; border-radius:6px;">
        <button type="submit" class="btn-green">Create</button>
    </form>
</div>

<%-- Folder grid --%>
<%
    java.util.List<com.hikebuddy.model.StoryFolder> folders =
            (java.util.List<com.hikebuddy.model.StoryFolder>) request.getAttribute("folders");
%>

<% if (folders != null && !folders.isEmpty()) { %>
<div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(220px, 1fr)); gap:20px;">
    <%
        for (com.hikebuddy.model.StoryFolder folder : folders) {
    %>
    <div style="background:white; border:1px solid #dce8dc; border-radius:8px; overflow:hidden;">
        <a href="${pageContext.request.contextPath}/folder?folderId=<%= folder.getId() %>"
           style="text-decoration:none; color:inherit;">
            <% if (folder.getThumbnailPath() != null) { %>
            <img src="${pageContext.request.contextPath}<%= folder.getThumbnailPath() %>"
                 style="width:100%; height:160px; object-fit:cover; display:block;">
            <% } else { %>
            <div style="width:100%; height:160px; background:#f4f6f4; display:flex;
                                align-items:center; justify-content:center; color:#aaa;">
                No photos yet
            </div>
            <% } %>
            <div style="padding:12px;">
                <strong><%= escapeHtml(folder.getName()) %></strong>
                <div style="font-size:0.8em; color:#888; margin-top:4px;">
                    <%= folder.getCreatedAt() != null ? folder.getCreatedAt().toString().substring(0, 10) : "" %>
                </div>
            </div>
        </a>
        <form method="post" action="${pageContext.request.contextPath}/storyboard"
              style="padding:0 12px 12px;"
              onsubmit="return confirm('Delete this folder and all its photos?')">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="folderId" value="<%= folder.getId() %>">
            <button type="submit" class="btn-red"
                    style="width:100%; background-color:#c0392b; color:white; border:none;
                   padding:8px; border-radius:6px; cursor:pointer;">
                Delete folder
            </button>
        </form>
    </div>
    <%
        }
    %>
</div>
<% } else { %>
<p style="color:#aaa;">No folders yet. Your folders will appear here when you log a hike.</p>
<% } %>

<%@ include file="footer.jsp" %>