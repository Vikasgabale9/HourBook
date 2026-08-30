package com.app.hourbook.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler)
            throws Exception {

        Object admin =
                request.getSession()
                       .getAttribute("ADMIN");

        if(admin == null) {

            response.sendRedirect(
                    "/admin/login");

            return false;
        }

        return true;
    }
}