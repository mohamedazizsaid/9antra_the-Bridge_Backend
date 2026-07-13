package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(int userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.readStatus = false")
    long countUnreadByUserId(@Param("userId") int userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.readStatus = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") int userId);
}
