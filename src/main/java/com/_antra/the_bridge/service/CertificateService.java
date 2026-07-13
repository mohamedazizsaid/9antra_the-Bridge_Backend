package com._antra.the_bridge.service;

import com._antra.the_bridge.dto.CertificateDTO;

import java.util.List;

public interface CertificateService {
    List<CertificateDTO> getCertificatesByStudent(int studentId);
    CertificateDTO verifyCertificate(String hash);
    CertificateDTO generateCertificate(int studentId, Long phaseId);
    byte[] downloadCertificatePdf(String certificateNumber);
}

