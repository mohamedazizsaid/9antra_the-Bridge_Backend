package com._antra.the_bridge.controller;

import com._antra.the_bridge.dto.CertificateDTO;
import com._antra.the_bridge.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Certificats", description = "Endpoints pour l'émission et la vérification des certificats blockchain")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Certificats obtenus par un stagiaire")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByStudent(@PathVariable int studentId) {
        return ResponseEntity.ok(certificateService.getCertificatesByStudent(studentId));
    }

    @GetMapping("/verify/{hash}")
    @Operation(summary = "Vérifier l'authenticité d'un certificat par hash ou numéro")
    public ResponseEntity<CertificateDTO> verifyCertificate(@PathVariable String hash) {
        return ResponseEntity.ok(certificateService.verifyCertificate(hash));
    }

    @GetMapping("/download/{certificateNumber}")
    @Operation(summary = "Télécharger le certificat en format PDF professionnel")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String certificateNumber) {
        byte[] pdfBytes = certificateService.downloadCertificatePdf(certificateNumber);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + certificateNumber + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
