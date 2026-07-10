<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<div style="max-width: 420px; margin: 40px auto;">
    <h1>Log in</h1>
    <p style="margin-bottom: 20px;">Don't have an account?
        <a href="${pageContext.request.contextPath}/register"
           style="color: #2d6a4f; font-weight: 500;">Register</a>
    </p>

    <%-- Success message after registration --%>
    <% if ("registered".equals(request.getParameter("success"))) { %>
    <div style="background-color: #ecfdf5; color: #065f46; border: 1px solid #a7f3d0;
                    padding: 12px 16px; border-radius: 6px; margin-bottom: 16px;">
        Account created! You can now log in.
    </div>
    <% } %>

    <%-- Error message if login failed --%>
    <% if (request.getAttribute("error") != null) { %>
    <div style="background-color: #fdecea; color: #b91c1c; border: 1px solid #f5c6c6;
                    padding: 12px 16px; border-radius: 6px; margin-bottom: 16px;">
        <%= request.getAttribute("error") %>
    </div>
    <% } %>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <div style="margin-bottom: 16px;">
            <label style="display: block; font-weight: 500; margin-bottom: 6px;">
                Username
            </label>
            <input type="text"
                   name="username"
                   required
                   value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>"
                   style="width: 100%; padding: 10px 12px; border: 1px solid #ccc;
                          border-radius: 6px; font-size: 15px;">
        </div>
        <div style="margin-bottom: 24px;">
            <label style="display: block; font-weight: 500; margin-bottom: 6px;">
                Password
            </label>
            <input type="password"
                   name="password"
                   required
                   style="width: 100%; padding: 10px 12px; border: 1px solid #ccc;
                          border-radius: 6px; font-size: 15px;">
        </div>
        <button type="submit"
                style="width: 100%; padding: 11px; background-color: #2d6a4f;
                       color: white; border: none; border-radius: 6px;
                       font-size: 15px; font-weight: 500; cursor: pointer;">
            Log in
        </button>
    </form>
</div>
<%@ include file="footer.jsp" %>