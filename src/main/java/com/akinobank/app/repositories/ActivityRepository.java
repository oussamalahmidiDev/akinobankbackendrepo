package com.akinobank.app.repositories;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Activity;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface  ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findAllByUserRole(Role role, Pageable pageable);
    List<Activity> findAllByUserRoleAndUser_Agent_Agence(Role role, Agence agence , Pageable pageable);
    List<Activity> findAllByUserAndUser_Agent_Agence(User user, Agence agence, Pageable pageable);
    List<Activity> findAllByUserId(Long id, Pageable pageable);
    List<Activity> findAllByUserRole(Role role);
    List<Activity> findAllByUser(User user, Pageable pagebale);

    List<Activity> findAllByUserRoleIsNot(Role role, Pageable pageable);
}
