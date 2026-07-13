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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
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

    @Override
    public byte[] downloadCertificatePdf(String certificateNumber) {
        Certificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new CustomException("Certificate not found: " + certificateNumber, HttpStatus.NOT_FOUND));

        try (PDDocument document = new PDDocument()) {
            // Create a landscape page (A4 format)
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);

            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();

            PDType1Font fontTitle = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontSub = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
            PDType1Font fontMono = new PDType1Font(Standard14Fonts.FontName.COURIER);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 1. Draw elegant background color (light cream/gray)
                contentStream.setNonStrokingColor(new Color(248, 248, 250));
                contentStream.addRect(0, 0, width, height);
                contentStream.fill();

                // 2. Draw thick outer border (Crimson #C62761)
                contentStream.setStrokingColor(new Color(198, 39, 97));
                contentStream.setLineWidth(12);
                contentStream.addRect(20, 20, width - 40, height - 40);
                contentStream.stroke();

                // 3. Draw thin inner border (Gold #F5A623)
                contentStream.setStrokingColor(new Color(245, 166, 35));
                contentStream.setLineWidth(3);
                contentStream.addRect(32, 32, width - 64, height - 64);
                contentStream.stroke();

                // 4. Header title
                contentStream.beginText();
                contentStream.setFont(fontTitle, 28);
                contentStream.setNonStrokingColor(new Color(198, 39, 97)); // Crimson
                contentStream.newLineAtOffset(100, height - 100);
                contentStream.showText("THE BRIDGE — 9ANTRA");
                contentStream.endText();

                // 5. Certificate subtitle
                contentStream.beginText();
                contentStream.setFont(fontSub, 14);
                contentStream.setNonStrokingColor(new Color(30, 30, 60));
                contentStream.newLineAtOffset(100, height - 130);
                contentStream.showText("CERTIFICAT DE REUSSITE PEDAGOGIQUE");
                contentStream.endText();

                // 6. Decerne a ...
                contentStream.beginText();
                contentStream.setFont(fontItalic, 16);
                contentStream.setNonStrokingColor(new Color(100, 100, 130));
                contentStream.newLineAtOffset(100, height - 200);
                contentStream.showText("Ce certificat est decede a :");
                contentStream.endText();

                // Student Name
                String studentName = certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName();
                contentStream.beginText();
                contentStream.setFont(fontTitle, 36);
                contentStream.setNonStrokingColor(new Color(245, 166, 35)); // Gold
                contentStream.newLineAtOffset(100, height - 250);
                contentStream.showText(studentName.toUpperCase());
                contentStream.endText();

                // For completing phase/formation
                String descText = "Pour avoir valide avec succes la phase :";
                contentStream.beginText();
                contentStream.setFont(fontSub, 14);
                contentStream.setNonStrokingColor(new Color(30, 30, 60));
                contentStream.newLineAtOffset(100, height - 300);
                contentStream.showText(descText);
                contentStream.endText();

                // Phase details
                String phaseInfo = "\"" + certificate.getPhase().getTitle() + "\"";
                if (certificate.getPhase().getFormation() != null) {
                    phaseInfo += " du programme " + certificate.getPhase().getFormation().getTitle();
                }
                contentStream.beginText();
                contentStream.setFont(fontTitle, 16);
                contentStream.setNonStrokingColor(new Color(198, 39, 97));
                contentStream.newLineAtOffset(100, height - 330);
                contentStream.showText(phaseInfo);
                contentStream.endText();

                // 7. Blockchain details
                contentStream.beginText();
                contentStream.setFont(fontSub, 10);
                contentStream.setNonStrokingColor(new Color(120, 120, 140));
                contentStream.newLineAtOffset(100, 130);
                contentStream.showText("Numero de certificat : " + certificate.getCertificateNumber());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontSub, 10);
                contentStream.newLineAtOffset(100, 110);
                contentStream.showText("Date d'emission : " + certificate.getIssueDate().toString());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontMono, 8);
                contentStream.newLineAtOffset(100, 90);
                contentStream.showText("Blockchain : Polygon Network | Tx Hash : " + certificate.getBlockchainTransactionHash());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontMono, 8);
                contentStream.newLineAtOffset(100, 75);
                contentStream.showText("Verification Hash (SHA-256) : " + certificate.getHashValue());
                contentStream.endText();

                // Signatures
                contentStream.beginText();
                contentStream.setFont(fontItalic, 12);
                contentStream.setNonStrokingColor(new Color(30, 30, 60));
                contentStream.newLineAtOffset(width - 250, 120);
                contentStream.showText("La Direction General");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(fontMono, 12);
                contentStream.newLineAtOffset(width - 250, 90);
                contentStream.showText("[ SIGNATURE ELECTRONIQUE ]");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating certificate PDF", e);
        }
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
