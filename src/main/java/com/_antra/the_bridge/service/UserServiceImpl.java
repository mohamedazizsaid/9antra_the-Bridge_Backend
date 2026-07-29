package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.dto.DashboardStatsDTO;
import com._antra.the_bridge.dto.UserDTO;
import com._antra.the_bridge.entity.User;
import com._antra.the_bridge.enumType.Role;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           FormationRepository formationRepository,
                           EnrollmentRepository enrollmentRepository,
                           CertificateRepository certificateRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.formationRepository = formationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.certificateRepository = certificateRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        return DTOHelper.toDTO(user);
    }

    @Override
    public UserDTO updateProfile(String email, UserDTO profileDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        user.setFirstName(profileDTO.getFirstName());
        user.setLastName(profileDTO.getLastName());
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getAge() != null && profileDTO.getAge() > 0) {
            user.setAge(profileDTO.getAge());
        }
        if (profileDTO.getAvatar() != null) {
            user.setAvatar(profileDTO.getAvatar());
        }

        userRepository.save(user);
        return DTOHelper.toDTO(user);
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException("Mot de passe actuel incorrect", HttpStatus.BAD_REQUEST);
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new CustomException("Le nouveau mot de passe doit contenir au moins 8 caractères", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public DashboardStatsDTO getAdminStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (user.getRole() != Role.ADMIN) {
            throw new CustomException("Unauthorized", HttpStatus.FORBIDDEN);
        }

        long totalUsers = userRepository.count();
        long totalFormations = formationRepository.count();
        long totalStagiaires = userRepository.findAll().stream().filter(u -> u.getRole() == Role.STAGIAIRE).count();
        long totalFormateurs = userRepository.findAll().stream().filter(u -> u.getRole() == Role.FORMATEUR).count();
        long totalEnrollments = enrollmentRepository.count();
        long totalCertificates = certificateRepository.count();
        long unreadNotifications = notificationRepository.countUnreadByUserId(user.getId());

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalFormations(totalFormations)
                .totalStagiaires(totalStagiaires)
                .totalFormateurs(totalFormateurs)
                .totalEnrollments(totalEnrollments)
                .totalCertificates(totalCertificates)
                .unreadNotifications(unreadNotifications)
                .build();
    }
}
