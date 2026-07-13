package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.DashboardStatsDTO;
import com._antra.the_bridge.dto.UserDTO;
import com._antra.the_bridge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "Endpoints pour la gestion des profils et utilisateurs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Liste de tous les utilisateurs (Admin)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    @Operation(summary = "Profil de l'utilisateur connecté")
    public ResponseEntity<UserDTO> getMyProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @PutMapping("/me")
    @Operation(summary = "Mise à jour du profil")
    public ResponseEntity<UserDTO> updateMyProfile(Principal principal, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), userDTO));
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques d'administration global (Admin)")
    public ResponseEntity<DashboardStatsDTO> getAdminStats(Principal principal) {
        return ResponseEntity.ok(userService.getAdminStats(principal.getName()));
    }
}
