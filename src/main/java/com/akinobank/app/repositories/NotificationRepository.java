package com.akinobank.app.repositories;

import com.akinobank.app.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;


//@RepositoryRestController
public interface NotificationRepository extends JpaRepository<Notification,Long> {
}
