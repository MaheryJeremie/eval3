package com.example.evalrh.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();

        boolean isLoginPage = requestURI.equals("/") || requestURI.equals("/login");
        boolean isStaticResource = requestURI.startsWith("/static/") || requestURI.startsWith("/css/") || requestURI.startsWith("/js/");

        if (isLoginPage || isStaticResource) {
            chain.doFilter(request, response);
            return;
        }

        if (session == null || session.getAttribute("sid") == null) {
            httpResponse.sendRedirect("/");
            return;
        }

        chain.doFilter(request, response);
    }
}
