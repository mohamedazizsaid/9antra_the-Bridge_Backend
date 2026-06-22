package com._antra.the_bridge.service;

import com._antra.the_bridge.config.JwtUtils;
import com._antra.the_bridge.dto.*;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.enumType.Status;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtUtils             jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CloudinaryService    cloudinaryService;
    private final MailService          mailService;
    private final OAuthService         oauthService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils,
                           AuthenticationManager authenticationManager,
                           CloudinaryService cloudinaryService,
                           MailService mailService,
                           OAuthService oauthService) {
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.jwtUtils             = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.cloudinaryService    = cloudinaryService;
        this.mailService          = mailService;
        this.oauthService         = oauthService;
    }

    // ─── REGISTER ────────────────────────────────────────────────────────────

    @Override
    public AuthResponse register(RegisterRequest request, MultipartFile avatar) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Cet email est déjà utilisé", HttpStatus.BAD_REQUEST);
        }

        // Upload avatar to Cloudinary (optional)
        String avatarUrl = null;
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = cloudinaryService.uploadAvatar(avatar);
        }

        // Generate 6-digit verification code
        String verificationCode = generateCode();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAge(request.getAge());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAvatar(avatarUrl);
        user.setRole(Role.STAGIAIRE);           // Always STAGIAIRE on register
        user.setStatus(Status.PENDING);          // Pending until email verified
        user.setEmailVerified(false);
        user.setVerificationCode(verificationCode);
        user.setAuthProvider("LOCAL");
        // createdAt / lastActivity set by @PrePersist

        userRepository.save(user);

        // Send verification email (async)
        mailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationCode);

        // Return response WITHOUT token — user must verify email first
        return buildResponse(null, user);
    }

    // ─── VERIFY EMAIL ─────────────────────────────────────────────────────────

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new CustomException("Email déjà vérifié", HttpStatus.BAD_REQUEST);
        }

        if (!request.getCode().equals(user.getVerificationCode())) {
            throw new CustomException("Code de vérification incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setEmailVerified(true);
        user.setStatus(Status.ACTIVE);
        user.setVerificationCode(null);
        userRepository.save(user);
    }

    // ─── RESEND CODE ──────────────────────────────────────────────────────────

    @Override
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new CustomException("Email déjà vérifié", HttpStatus.BAD_REQUEST);
        }

        String newCode = generateCode();
        user.setVerificationCode(newCode);
        userRepository.save(user);

        mailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), newCode);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));

        if (!user.isEmailVerified()) {
            throw new CustomException("Veuillez vérifier votre email avant de vous connecter", HttpStatus.FORBIDDEN);
        }

        user.setLastActivity(LocalDate.now());
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());
        return buildResponse(token, user);
    }

    // ─── OAUTH LOGIN (Google / Facebook) ──────────────────────────────────────

    @Override
    public AuthResponse oauthLogin(OAuthLoginRequest request) {
        Map<String, String> info;

        switch (request.getProvider().toUpperCase()) {
            case "GOOGLE"   -> info = oauthService.verifyGoogleToken(request.getAccessToken());
            case "FACEBOOK" -> info = oauthService.verifyFacebookToken(request.getAccessToken());
            default -> throw new CustomException("Fournisseur OAuth non supporté: " + request.getProvider(), HttpStatus.BAD_REQUEST);
        }

        String email      = info.get("email");
        String firstName  = info.get("firstName");
        String lastName   = info.get("lastName");
        String avatar     = info.get("avatar");
        String providerId = info.get("providerId");

        if (email == null || email.isBlank()) {
            throw new CustomException("Impossible de récupérer l'email depuis " + request.getProvider(), HttpStatus.BAD_REQUEST);
        }

        // Find or create user
        Optional<User> existing = userRepository.findByEmail(email);
        User user;

        if (existing.isPresent()) {
            // Update last activity
            user = existing.get();
            user.setLastActivity(LocalDate.now());
            userRepository.save(user);
        } else {
            // Create new STAGIAIRE user from OAuth
            user = new User();
            user.setFirstName(firstName.isBlank() ? "User" : firstName);
            user.setLastName(lastName.isBlank() ? "" : lastName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(generateCode() + System.currentTimeMillis()));
            user.setAvatar(avatar);
            user.setRole(Role.STAGIAIRE);
            user.setStatus(Status.ACTIVE);
            user.setEmailVerified(true);           // OAuth emails are pre-verified
            user.setAuthProvider(request.getProvider().toUpperCase());
            user.setProviderId(providerId);
            userRepository.save(user);
        }

        String token = jwtUtils.generateToken(user.getEmail());
        return buildResponse(token, user);
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private AuthResponse buildResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .age(user.getAge())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .authProvider(user.getAuthProvider())
                .build();
    }
}

