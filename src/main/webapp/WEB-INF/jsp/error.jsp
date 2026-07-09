<%@ include file="/jsp/header.jsp" %>

<%
    Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    String title;
    String message;
    if (statusCode != null && statusCode == 404) {
        title = "Page Not Found";
        message = "Oops! Page not found.";
    } else {
        title = "Something Went Wrong";
        message = "Something went wrong. Please try again later.";
    }
%>

<div class="section-card" style="text-align:center; margin-top:60px;">
    <h1><%= title %></h1>
    <p style="color:#666; margin:16px 0;"><%= message %></p>
    <a href="${pageContext.request.contextPath}/home"
       style="display:inline-block; padding:10px 24px; background:#2d6a4f; color:white;
              text-decoration:none; border-radius:6px; font-weight:500;">
        Back to Home
    </a>
</div>

<%@ include file="/jsp/footer.jsp" %>