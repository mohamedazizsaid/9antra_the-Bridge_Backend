package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.CertificateDTO;
import com._antra.the_bridge.dto.DTOHelper;
import com._antra.the_bridge.entity.Certificate;
import com._antra.the_bridge.exception.CustomException;
import com._antra.the_bridge.repository.CertificateRepository;
import com._antra.the_bridge.repository.UserRepository;
import com._antra.the_bridge.repository.PhaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final PhaseRepository phaseRepository;

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  UserRepository userRepository,
                                  PhaseRepository phaseRepository) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.phaseRepository = phaseRepository;
    }

    @Override
    public List<CertificateDTO> getCertificatesByStudent(int studentId) {
        return certificateRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CertificateDTO verifyCertificate(String hash) {
        Certificate certificate = certificateRepository.findByHashValue(hash)
                .or(() -> certificateRepository.findByCertificateNumber(hash))
                .orElseThrow(() -> new CustomException("Certificate not found", HttpStatus.NOT_FOUND));
        return DTOHelper.toDTO(certificate);
    }

    @Override
    public CertificateDTO generateCertificate(int studentId, Long phaseId) {
        com._antra.the_bridge.entity.User student = userRepository.findById(studentId)
                .orElseThrow(() -> new CustomException("Student not found", HttpStatus.NOT_FOUND));
        com._antra.the_bridge.entity.Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new CustomException("Phase not found", HttpStatus.NOT_FOUND));

        String certNum = "CERT-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Generate simulated Polygon transaction hash and SHA-256 hash value
        String blockchainTx = "0x" + generateRandomHex(64);
        String certHash = generateSha256(student.getEmail() + ":" + phase.getTitle() + ":" + certNum);

        Certificate certificate = new Certificate();
        certificate.setCertificateNumber(certNum);
        certificate.setPdfUrl("/api/certificates/download/" + certNum);
        certificate.setHashValue(certHash);
        certificate.setBlockchainTransactionHash(blockchainTx);
        certificate.setIssueDate(java.time.LocalDate.now());
        certificate.setStudent(student);
        certificate.setPhase(phase);

        certificateRepository.save(certificate);
        return DTOHelper.toDTO(certificate);
    }

    private String generateRandomHex(int length) {
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.toString().substring(0, length);
    }

    private String generateSha256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating SHA-256 hash", e);
        }
    }
}
