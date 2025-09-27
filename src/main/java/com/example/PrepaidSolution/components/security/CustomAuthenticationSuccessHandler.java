package com.example.PrepaidSolution.components.security;

import com.example.PrepaidSolution.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper ;

    public CustomAuthenticationSuccessHandler(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        String redirectUrl = "/";

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ROLE_"+Role.ADMIN.toString())) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (grantedAuthority.getAuthority().equals("ROLE_"+Role.OWNER.toString())) {
                redirectUrl = "/owner/dashboard";
                break;
            } else if (grantedAuthority.getAuthority().equals("ROLE_"+Role.TENANT.toString())) {
                redirectUrl = "/tenant/dashboard";
                break;
            }
        }

        //response.sendRedirect(redirectUrl);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("redirectUrl", redirectUrl);

        PrintWriter writer = response.getWriter();
        objectMapper.writeValue(writer, responseBody);
        writer.flush();
    }
}