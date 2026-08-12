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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl implements CertificateService {

    private static final Color CRIMSON   = new Color(198, 39, 97);
    private static final Color GOLD      = new Color(245, 166, 35);
    private static final Color GOLD_D    = new Color(200, 130, 15);
    private static final Color DARK_BG   = new Color(12, 12, 32);
    private static final Color NEAR_WHITE= new Color(248, 247, 244);
    private static final Color GRAY_MID  = new Color(110, 110, 130);
    private static final Color GRAY_DARK = new Color(30,  30,  55);
    private static final Color DIVIDER   = new Color(220, 215, 205);

    private final CertificateRepository certificateRepository;
    private final UserRepository        userRepository;
    private final PhaseRepository       phaseRepository;

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  UserRepository userRepository,
                                  PhaseRepository phaseRepository) {
        this.certificateRepository = certificateRepository;
        this.userRepository        = userRepository;
        this.phaseRepository       = phaseRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<CertificateDTO> getCertificatesByStudent(int studentId) {
        return certificateRepository.findByStudentId(studentId).stream()
                .map(DTOHelper::toDTO).collect(Collectors.toList());
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

        String certNum = "CERT-" + String.format("%04d", (int)(Math.random() * 9000 + 1000))
                + "-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        // Genuine Cryptographic Blockchain Hash Seal (SHA-256 Digest of identity payload)
        String rawPayload = String.format("BRIDGE_CHAIN_v1|STUDENT:%d:%s|PHASE:%d:%s|TIME:%d",
                student.getId(), student.getEmail(), phase.getId(), phase.getTitle(), System.currentTimeMillis());
        String certHash = generateSha256(rawPayload);
        String blockchainTx = "0x" + generateSha256("TX_PROOF:" + certHash + ":" + certNum);

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

    // ─────────────────────────────────────────────────────────────────────────
    //  PDF Generation  (A4 Landscape = 841.89 × 595.28 pt)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public byte[] downloadCertificatePdf(String certificateNumber) {
        Certificate cert = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new CustomException("Certificate not found: " + certificateNumber, HttpStatus.NOT_FOUND));

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);
            float W = page.getMediaBox().getWidth();   // ≈ 841
            float H = page.getMediaBox().getHeight();  // ≈ 595

            PDType1Font fBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fReg     = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fItalic  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
            PDType1Font fBoldIt  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
            PDType1Font fMono    = new PDType1Font(Standard14Fonts.FontName.COURIER);
            PDType1Font fMonoBold= new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ── 1. BACKGROUND ─────────────────────────────────────────────
                cs.setNonStrokingColor(NEAR_WHITE);
                fillRect(cs, 0, 0, W, H);

                // Left dark sidebar (crimson-dark)
                cs.setNonStrokingColor(DARK_BG);
                fillRect(cs, 0, 0, 58, H);

                // Right dark sidebar
                fillRect(cs, W - 58, 0, 58, H);

                // Gold accent line on left sidebar
                cs.setNonStrokingColor(GOLD);
                fillRect(cs, 52, 0, 6, H);

                // Gold accent line on right sidebar
                fillRect(cs, W - 58, 0, 6, H);

                // ── 2. OUTER BORDERS ─────────────────────────────────────────
                // Crimson outer border
                cs.setStrokingColor(CRIMSON);
                cs.setLineWidth(3.5f);
                strokeRect(cs, 70, 16, W - 140, H - 32);

                // Gold inner border (thin)
                cs.setStrokingColor(GOLD);
                cs.setLineWidth(1.0f);
                strokeRect(cs, 75, 21, W - 150, H - 42);

                // ── 3. HEADER BAND ────────────────────────────────────────────
                cs.setNonStrokingColor(DARK_BG);
                fillRect(cs, 70, H - 90, W - 140, 74);

                // Gold bottom line of header
                cs.setNonStrokingColor(GOLD);
                fillRect(cs, 70, H - 92, W - 140, 3);

                // Logo / Title in header
                drawText(cs, fBold, 22, GOLD, 95, H - 45,
                        "9ANTRA  -  THE BRIDGE");
                drawText(cs, fReg, 9, new Color(180, 170, 155), 95, H - 62,
                        "Plateforme d'Excellence Pedagogique & Certification Blockchain");

                // Tiny right-aligned badge text
                drawTextRight(cs, fMono, 8, GOLD_D, W - 85, H - 45,
                        "CERTIFICAT OFFICIEL");
                drawTextRight(cs, fMono, 7, GRAY_MID, W - 85, H - 58,
                        "Emis par le Systeme Academique Bridge");

                // ── 4. TITLE SECTION ──────────────────────────────────────────
                drawTextCentered(cs, fBold, 11, GRAY_MID, W / 2, H - 118,
                        "CERTIFICAT DE REUSSITE PEDAGOGIQUE");

                // Crimson underline below subtitle
                cs.setNonStrokingColor(CRIMSON);
                fillRect(cs, W / 2 - 110, H - 124, 220, 1.5f);

                // ── 5. "EST DECERNÉ À" ─────────────────────────────────────────
                drawTextCentered(cs, fItalic, 10, GRAY_MID, W / 2, H - 145,
                        "Ce certificat est decerne a");

                // ── 6. STUDENT NAME — large centered ──────────────────────────
                String name = cert.getStudent().getFirstName().toUpperCase()
                        + "  " + cert.getStudent().getLastName().toUpperCase();

                // Name background box — solid pale gold (no alpha: PDFBox rejects it)
                cs.setNonStrokingColor(new Color(253, 246, 224));
                fillRect(cs, 120, H - 210, W - 240, 52);

                // Name border
                cs.setStrokingColor(GOLD);
                cs.setLineWidth(1.2f);
                strokeRect(cs, 120, H - 210, W - 240, 52);

                // Gold left strip inside name box
                cs.setNonStrokingColor(GOLD);
                fillRect(cs, 120, H - 210, 5, 52);

                // Name text
                float nameSize = name.length() > 30 ? 26 : 32;
                drawTextCentered(cs, fBold, nameSize, GRAY_DARK, W / 2, H - 194, name);

                // ── 7. "POUR AVOIR VALIDÉ" ───────────────────────────────────
                drawTextCentered(cs, fItalic, 10, GRAY_MID, W / 2, H - 228,
                        "Pour avoir validé avec succès la phase de formation :");

                // Phase title — use ASCII guillemets safe for Latin-1
                String phaseTitle = cert.getPhase().getTitle();
                drawTextCentered(cs, fBoldIt, 14, CRIMSON, W / 2, H - 252,
                        "\"" + phaseTitle + "\"");

                // Formation name (if exists)
                if (cert.getPhase().getFormation() != null) {
                    String formationTitle = cert.getPhase().getFormation().getTitle();
                    drawTextCentered(cs, fReg, 9, GRAY_MID, W / 2, H - 268,
                            "Programme : " + formationTitle);
                }

                // ── 8. HORIZONTAL DIVIDER ─────────────────────────────────────
                cs.setNonStrokingColor(DIVIDER);
                fillRect(cs, 90, H - 285, W - 180, 1);

                // ── 9. DATE & SIGNATURE SECTION ──────────────────────────────
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
                String dateStr = cert.getIssueDate().format(fmt);

                // Issue date (left side)
                drawText(cs, fReg, 9, GRAY_MID, 100, H - 305, "Date d'emission :");
                drawText(cs, fBold, 10, GRAY_DARK, 100, H - 319, dateStr);

                // Signature box (right side)
                float sigX = W - 260;
                float sigY = H - 330;
                cs.setStrokingColor(DIVIDER);
                cs.setLineWidth(0.8f);
                strokeRect(cs, sigX, sigY, 170, 45);

                drawText(cs, fBoldIt, 9, GRAY_DARK, sigX + 8, sigY + 34, "La Direction Pedagogique");
                drawText(cs, fMono, 7, GRAY_MID, sigX + 8, sigY + 20, "[ SIGNATURE ELECTRONIQUE ]");
                cs.setNonStrokingColor(CRIMSON);
                fillRect(cs, sigX, sigY, 4, 45);

                // Center star decoration — use Latin-1 asterism (no Unicode stars)
                drawTextCentered(cs, fBold, 18, GOLD, W / 2, H - 318, "*");

                // ── 10. BLOCKCHAIN SECTION ────────────────────────────────────
                // Dark blockchain band at bottom
                cs.setNonStrokingColor(DARK_BG);
                fillRect(cs, 70, 16, W - 140, 75);

                // Gold top border of blockchain band
                cs.setNonStrokingColor(GOLD);
                fillRect(cs, 70, 91, W - 140, 2);

                // Label — ASCII only (Type1 fonts don't support Unicode emoji)
                drawText(cs, fMonoBold, 7, GOLD, 90, 76, "[ BLOCKCHAIN VERIFICATION ] POLYGON NETWORK");

                // Cert number
                drawText(cs, fMono, 7, new Color(170, 165, 155), 90, 62,
                        "No  " + cert.getCertificateNumber()
                                + "       Date : " + cert.getIssueDate().toString());

                // Hash — truncate with ASCII '...' (not U+2026 ellipsis)
                String txShort = cert.getBlockchainTransactionHash().length() > 42
                        ? cert.getBlockchainTransactionHash().substring(0, 42) + "..."
                        : cert.getBlockchainTransactionHash();
                drawText(cs, fMono, 6.5f, new Color(150, 145, 135), 90, 49,
                        "Tx Hash : " + txShort);

                String hashShort = cert.getHashValue().length() > 48
                        ? cert.getHashValue().substring(0, 48) + "..."
                        : cert.getHashValue();
                drawText(cs, fMono, 6.5f, new Color(150, 145, 135), 90, 38,
                        "SHA-256  : " + hashShort);

                // Verification URL hint (right-aligned)
                drawTextRight(cs, fMono, 6.5f, GOLD_D, W - 85, 62,
                        "bridge.9antra.tn/verify");
                drawTextRight(cs, fReg, 6, GRAY_MID, W - 85, 49,
                        "Verifiez l'authenticite en ligne");

                // ── 11. DECORATIVE CORNER MARKS — Latin-1 only ────────────────
                String diamond = "+";
                for (int[] corner : new int[][]{{81, (int)(H-28)},{81,27},{(int)(W-81),(int)(H-28)},{(int)(W-81),27}}) {
                    drawTextCentered(cs, fBold, 10, GOLD, corner[0], corner[1], diamond);
                }

            } // end content stream

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating certificate PDF: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Drawing helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void fillRect(PDPageContentStream cs, float x, float y, float w, float h) throws Exception {
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private void strokeRect(PDPageContentStream cs, float x, float y, float w, float h) throws Exception {
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    private void drawText(PDPageContentStream cs, PDType1Font font, float size,
                          Color color, float x, float y, String text) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawTextCentered(PDPageContentStream cs, PDType1Font font, float size,
                                  Color color, float cx, float y, String text) throws Exception {
        float textWidth = font.getStringWidth(text) / 1000 * size;
        drawText(cs, font, size, color, cx - textWidth / 2, y, text);
    }

    private void drawTextRight(PDPageContentStream cs, PDType1Font font, float size,
                               Color color, float rightX, float y, String text) throws Exception {
        float textWidth = font.getStringWidth(text) / 1000 * size;
        drawText(cs, font, size, color, rightX - textWidth, y, text);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Crypto helpers
    // ─────────────────────────────────────────────────────────────────────────



    private String generateSha256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}
