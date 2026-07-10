package com.hikebuddy.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        // These URLs are allowed without login
        boolean isPublic = uri.equals("/hikebuddy/login") ||
                uri.equals("/hikebuddy/register") ||
                uri.startsWith("/hikebuddy/css/");

        if (isPublic) {
            // Let the request through without checking session
            chain.doFilter(request, response);
            return;
        }

        // For all other URLs — check if user is logged in
        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            // Not logged in — redirect to login page
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        // Logged in — let the request through
        chain.doFilter(request, response);
    }
}