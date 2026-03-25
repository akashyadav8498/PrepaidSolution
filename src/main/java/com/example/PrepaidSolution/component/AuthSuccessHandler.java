package com.example.PrepaidSolution.component;

import com.example.PrepaidSolution.model.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        HttpSession session = request.getSession(true);
        session.setAttribute("username", authentication.getName());

        String redirectUrl = "/";

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority auth : authorities) {
            if (auth.getAuthority().equals("ROLE_ADMIN")) {
                redirectUrl = "/admin";
                session.setAttribute("role", Users.Role.ADMIN.toString());
                break;
            } else if (auth.getAuthority().equals("ROLE_OWNER")) {
                redirectUrl = "/owner";
                session.setAttribute("role", Users.Role.OWNER.toString());
                break;
            } else if (auth.getAuthority().equals("ROLE_TENANT")) {
                redirectUrl = "/tenant";
                session.setAttribute("role", Users.Role.TENANT.toString());
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
