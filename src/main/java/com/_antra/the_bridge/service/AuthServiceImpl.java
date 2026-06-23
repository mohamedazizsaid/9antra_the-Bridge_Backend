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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CloudinaryService cloudinaryService;
    private final MailService mailService;
    private final OAuthService oauthService;

    // Cache for pending registrations (email -> PendingRegistration)
    private final Map<String, PendingRegistration> pendingRegistrations = new HashMap<>();

    public AuthServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            AuthenticationManager authenticationManager,
            CloudinaryService cloudinaryService,
            MailService mailService,
            OAuthService oauthService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.cloudinaryService = cloudinaryService;
        this.mailService = mailService;
        this.oauthService = oauthService;
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

        // Store registration data temporarily (don't save to DB yet)
        PendingRegistration pending = new PendingRegistration(
                request.getFirstName(),
                request.getLastName(),
                request.getAge(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                avatarUrl,
                verificationCode);
        pendingRegistrations.put(request.getEmail(), pending);

        // Send verification email (async)
        mailService.sendVerificationEmail(request.getEmail(), request.getFirstName(), verificationCode);

        // Return response WITHOUT token — user must verify email first
        User tempUser = new User();
        tempUser.setFirstName(request.getFirstName());
        tempUser.setLastName(request.getLastName());
        tempUser.setEmail(request.getEmail());
        tempUser.setStatus(Status.PENDING);

        return buildResponse(null, tempUser);
    }

    // ─── VERIFY EMAIL ─────────────────────────────────────────────────────────

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        // Find pending registration
        PendingRegistration pending = pendingRegistrations.get(request.getEmail());
        if (pending == null) {
            throw new CustomException("Aucune demande d'inscription en attente pour cet email", HttpStatus.NOT_FOUND);
        }

        // Verify the code
        if (!request.getCode().equals(pending.verificationCode)) {
            throw new CustomException("Code de vérification incorrect", HttpStatus.BAD_REQUEST);
        }

        // Create user in DB from pending data
        User user = new User();
        user.setFirstName(pending.firstName);
        user.setLastName(pending.lastName);
        user.setAge(pending.age);
        user.setEmail(pending.email);
        user.setPassword(pending.encodedPassword);
        user.setPhone(pending.phone);
        user.setAvatar(pending.avatarUrl);
        user.setRole(Role.STAGIAIRE);
        user.setStatus(Status.ACTIVE);
        user.setEmailVerified(true);
        user.setAuthProvider("LOCAL");

        userRepository.save(user);

        // Clean up from cache
        pendingRegistrations.remove(request.getEmail());
    }

    // ─── RESEND CODE ──────────────────────────────────────────────────────────

    @Override
    public void resendVerificationCode(String email) {
        // Try to find in pending registrations first
        PendingRegistration pending = pendingRegistrations.get(email);

        if (pending != null) {
            // Generate new code for pending registration
            String newCode = generateCode();
            pending.verificationCode = newCode;

            // Send email with new code
            mailService.sendVerificationEmail(email, pending.firstName, newCode);
        } else {
            // Check if user exists and is not verified
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException("Utilisateur ou inscription en attente non trouvé",
                            HttpStatus.NOT_FOUND));

            if (user.isEmailVerified()) {
                throw new CustomException("Email déjà vérifié", HttpStatus.BAD_REQUEST);
            }

            String newCode = generateCode();
            user.setVerificationCode(newCode);
            userRepository.save(user);

            mailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), newCode);
        }
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

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
        if (request == null || request.getProvider() == null || request.getProvider().isBlank()) {
            throw new CustomException("Le fournisseur OAuth est obligatoire", HttpStatus.BAD_REQUEST);
        }
        if (request.getAccessToken() == null || request.getAccessToken().isBlank()) {
            throw new CustomException("Le jeton OAuth est manquant ou invalide", HttpStatus.BAD_REQUEST);
        }

        String provider = request.getProvider().trim().toUpperCase();
        Map<String, String> info;

        switch (provider) {
            case "GOOGLE" -> info = oauthService.verifyGoogleToken(request.getAccessToken());
            case "FACEBOOK" -> info = oauthService.verifyFacebookToken(request.getAccessToken());
            default -> throw new CustomException("Fournisseur OAuth non supporté: " + request.getProvider(),
                    HttpStatus.BAD_REQUEST);
        }

        String email = info.get("email");
        String firstName = info.get("firstName");
        String lastName = info.get("lastName");
        String avatar = info.get("avatar");
        String providerId = info.get("providerId");

        if (email == null || email.isBlank()) {
            throw new CustomException("Impossible de récupérer l'email depuis " + provider,
                    HttpStatus.BAD_REQUEST);
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
            user.setEmailVerified(true); // OAuth emails are pre-verified
            user.setAuthProvider(provider);
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

    // ─── FORGOT PASSWORD ──────────────────────────────────────────────────────

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé avec cet email", HttpStatus.NOT_FOUND));

        String resetCode = generateCode();
        user.setVerificationCode(resetCode);
        userRepository.save(user);

        mailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetCode);
    }

    // ─── RESET PASSWORD ───────────────────────────────────────────────────────

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            throw new CustomException("Code de réinitialisation invalide ou expiré", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationCode(null);
        userRepository.save(user);
    }

    // ─── PENDING REGISTRATION CLASS ───────────────────────────────────────────

    /**
     * Temporary storage for registration data before email verification
     */
    private static class PendingRegistration {
        String firstName;
        String lastName;
        int age;
        String email;
        String encodedPassword;
        String phone;
        String avatarUrl;
        String verificationCode;

        PendingRegistration(String firstName, String lastName, int age, String email,
                String encodedPassword, String phone, String avatarUrl, String verificationCode) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.email = email;
            this.encodedPassword = encodedPassword;
            this.phone = phone;
            this.avatarUrl = avatarUrl;
            this.verificationCode = verificationCode;
        }
    }
}
