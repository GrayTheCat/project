package com.epam.finaltask.service;

public interface PasswordResetService {
    void createAndSendResetToken(String email, String appUrl);
    boolean validateTokenAndResetPassword(String token, String newPassword);
}