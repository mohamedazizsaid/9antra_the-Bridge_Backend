package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.*;
import com._antra.the_bridge.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints d'inscription, connexion et vérification email")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─── REGISTER (multipart: data + optional avatar) ────────────────────────
    @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_JSON_VALUE })
    @Operation(summary = "Inscrire un stagiaire", description = "Crée un compte STAGIAIRE et envoie un email de vérification. Avatar uploadé sur Cloudinary.")
    public ResponseEntity<AuthResponse> register(
            @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return ResponseEntity.ok(authService.register(request, avatar));
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie l'utilisateur et retourne un JWT.")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ─── VERIFY EMAIL ─────────────────────────────────────────────────────────
    @PostMapping("/verify-email")
    @Operation(summary = "Vérifier l'email", description = "Valide le code à 6 chiffres reçu par email.")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity
                .ok(Map.of("message", "Email vérifié avec succès. Vous pouvez maintenant vous connecter."));
    }

    // ─── RESEND CODE ──────────────────────────────────────────────────────────
    @PostMapping("/resend-code")
    @Operation(summary = "Renvoyer le code", description = "Génère un nouveau code et le renvoie par email.")
    public ResponseEntity<Map<String, String>> resendCode(@RequestBody Map<String, String> body) {
        authService.resendVerificationCode(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Un nouveau code a été envoyé à votre adresse email."));
    }

    // ─── FORGOT PASSWORD ─────────────────────────────────────────────────────
    @PostMapping("/forgot-password")
    @Operation(summary = "Demander une réinitialisation", description = "Envoie un code de réinitialisation au stagiaire.")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Un code de réinitialisation a été envoyé à votre adresse email."));
    }

    // ─── RESET PASSWORD ──────────────────────────────────────────────────────
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe", description = "Vérifie le code et met à jour le mot de passe.")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
    }

    // ─── OAUTH LOGIN (Google / Facebook) ──────────────────────────────────────
    @PostMapping("/oauth/login")
    @Operation(summary = "Connexion sociale (Google / Facebook)", description = "Vérifie le token OAuth2 et connecte ou crée le compte STAGIAIRE.")
    public ResponseEntity<AuthResponse> oauthLogin(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authService.oauthLogin(request));
    }

    // ─── LOGOUT (client-side — JWT is stateless, just return 200) ────────────
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Confirme la déconnexion côté serveur. Le client doit supprimer le token.")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie."));
    }
}
