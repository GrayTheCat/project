package com.epam.finaltask.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import java.io.IOException;

public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorKey = "login.error";
        if (exception instanceof LockedException) {
            errorKey = "user.blocked";
        }
        getRedirectStrategy().sendRedirect(request, response, "/auth/sign-in?error=" + errorKey);
    }
}
