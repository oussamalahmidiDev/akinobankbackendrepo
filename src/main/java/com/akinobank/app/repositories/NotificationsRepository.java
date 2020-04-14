package com.akinobank.app.repositories;

import com.akinobank.app.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface NotificationsRepository extends JpaRepository<Notification,Integer> {
}
