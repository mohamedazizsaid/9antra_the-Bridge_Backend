package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByStudentId(int studentId);

    List<Evaluation> findByTrainerId(int trainerId);

    List<Evaluation> findByPhaseId(Long phaseId);

    Optional<Evaluation> findByStudentIdAndPhaseId(int studentId, Long phaseId);

    @Query("SELECT AVG(e.grade) FROM Evaluation e WHERE e.student.id = :studentId")
    Double findAverageGradeByStudentId(@Param("studentId") int studentId);
}
