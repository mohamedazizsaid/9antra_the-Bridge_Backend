package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Formation;
import com._antra.the_bridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, Long> {

    @Query("SELECT f FROM Formation f JOIN f.trainers t WHERE t = :trainer")
    List<Formation> findByTrainer(@Param("trainer") User trainer);

    @Query("SELECT DISTINCT f FROM Formation f JOIN f.enrollments e WHERE e.student = :student")
    List<Formation> findByStudent(@Param("student") User student);
}
