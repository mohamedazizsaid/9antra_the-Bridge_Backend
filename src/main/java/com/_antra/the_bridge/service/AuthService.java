package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    AuthResponse register(RegisterRequest request, MultipartFile avatar);
    AuthResponse login(LoginRequest request);
    void verifyEmail(VerifyEmailRequest request);
    void resendVerificationCode(String email);
    AuthResponse oauthLogin(OAuthLoginRequest request);
}

