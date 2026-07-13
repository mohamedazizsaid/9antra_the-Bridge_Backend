package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByStudentId(int studentId);

    Optional<Certificate> findByHashValue(String hashValue);

    Optional<Certificate> findByCertificateNumber(String certificateNumber);
}
