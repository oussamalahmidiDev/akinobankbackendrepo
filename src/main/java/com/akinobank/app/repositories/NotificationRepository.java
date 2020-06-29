package com.akinobank.app.repositories;

import com.akinobank.app.models.Notification;
import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;


//@RepositoryRestController
public interface NotificationRepository extends JpaRepository<Notification,Long> {

//    List<Notification> findAllByReceiver(User receiver, Pageable pageable);
//    List<Notification> findAllByReceiver(User receiver);

}
