package com.example.PrepaidSolution.component;

import com.example.PrepaidSolution.enums.Role;

import com.example.PrepaidSolution.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

    private final UserRepository userRepository;
    public CustomAuthenticationSuccessHandler(ObjectMapper objectMapper, UserRepository userRepository) {

        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        String redirectUrl = getRedirectionURL(request,authentication);

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


    private String getRedirectionURL(HttpServletRequest request, Authentication authentication){
        User userObj = (User) authentication.getPrincipal();
        String username = userObj.getUsername();
        com.example.PrepaidSolution.model.User user = userRepository.findByUsername(username);

        final HttpSession session = request.getSession();
        session.setAttribute("userName", user.getOwner().getName());
        session.setAttribute("userRole", user.getRole().toString());

        if (user.getRole().toString().equalsIgnoreCase("tenant")) return "/tenant";
        else return "/meter_management";
    }
}
