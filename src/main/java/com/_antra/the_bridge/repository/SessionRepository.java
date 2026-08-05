package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByPhaseId(Long phaseId);

    List<Session> findBySessionDate(LocalDate date);

    @Query("SELECT s FROM Session s WHERE s.sessionDate >= :today ORDER BY s.sessionDate ASC, s.startTime ASC")
    List<Session> findUpcoming(@Param("today") LocalDate today);

    @Query("SELECT s FROM Session s WHERE s.sessionDate = :today ORDER BY s.startTime ASC")
    List<Session> findToday(@Param("today") LocalDate today);

    @Query("SELECT s FROM Session s JOIN s.phase p JOIN p.formation f JOIN f.trainers t " +
           "WHERE t.id = :trainerId AND s.sessionDate = :today ORDER BY s.startTime ASC")
    List<Session> findTodayByTrainerId(@Param("trainerId") int trainerId, @Param("today") LocalDate today);

    @Query("SELECT s FROM Session s JOIN s.phase p JOIN p.formation f JOIN f.trainers t " +
           "WHERE t.id = :trainerId AND s.sessionDate >= :today ORDER BY s.sessionDate ASC, s.startTime ASC")
    List<Session> findUpcomingByTrainerId(@Param("trainerId") int trainerId, @Param("today") LocalDate today);

    @Query("SELECT s FROM Session s JOIN s.phase p JOIN p.formation f JOIN f.trainers t " +
           "WHERE t.id = :trainerId AND s.sessionDate < :today ORDER BY s.sessionDate DESC, s.startTime DESC")
    List<Session> findPastByTrainerId(@Param("trainerId") int trainerId, @Param("today") LocalDate today);

    @Query("SELECT s FROM Session s JOIN s.phase p JOIN p.formation f JOIN f.trainers t " +
           "WHERE t.id = :trainerId ORDER BY s.sessionDate DESC, s.startTime DESC")
    List<Session> findAllByTrainerId(@Param("trainerId") int trainerId);
}

