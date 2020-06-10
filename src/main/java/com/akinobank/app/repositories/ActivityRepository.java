package com.akinobank.app.repositories;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Activity;
import com.akinobank.app.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findAllByUserRole(Role role, Pageable pageable);
    List<Activity> findAllByUser(User user, Pageable pageable);

    List<Activity> findAllByUserRole(Role role);
    List<Activity> findAllByUser(User user);

}
