package com.akinobank.app.repositories;

import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrderByDateDeCreationDesc();

    User findOneByVerificationToken(String token);
}
