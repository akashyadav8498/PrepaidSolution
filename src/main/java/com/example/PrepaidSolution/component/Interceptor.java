package com.example.PrepaidSolution.component;

import com.example.PrepaidSolution.model.User;
import com.example.PrepaidSolution.repository.UserRepository;
import com.example.PrepaidSolution.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class Interceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String username = request.getParameter("username");
        String password = request.getParameter("password");


        log.info("interceptor---->");

        User user = userRepository.findByUsername(username);
        if (user == null || (!Utility.passwordEncoder.matches(password, user.getPassword())) ) {
            log.info("if mein ----> user: {}, password: {}" ,user,password);
            return false;
        }
        else {

            log.info("else mein ---->");
            String role = user.getRole().name();

            HttpSession session = request.getSession();
            session.setAttribute("userName", user.getUsername());
            session.setAttribute("role", role);

            return true;
        }
    }
}
