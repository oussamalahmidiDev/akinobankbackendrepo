package com.akinobank.app.repositories;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByRoleIsNotOrderByDateDeCreationDesc(Role role);

    List<User> findAllByRole(Role role);

    User findUserByRoleAndId(Role role , Long id);

    User deleteByIdAndRole(Long id , Role role);

//    User findUserByNomOrPrenom(String nom);

    User findOneByVerificationToken(String token);

    List<User> findUserByRoleAndNom(Role role, String clientName);

//    Optional<User> findByEmail(String email);

    User findByEmail(String email);
}
