package tn.esprit.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.forum.entity.UserStatus;

import java.util.List;

public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {

    @Query("SELECT s FROM UserStatus s WHERE s.userId IN :ids")
    List<UserStatus> findByUserIds(@Param("ids") List<Long> userIds);

    @Query("SELECT s FROM UserStatus s WHERE s.isOnline = true")
    List<UserStatus> findOnlineUsers();
}
