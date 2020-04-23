package com.akinobank.app.repositories;

import com.akinobank.app.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.stereotype.Repository;

@RepositoryRestController
public interface NotificationRepository extends JpaRepository<Notification,Long> {
}
