package com.akinobank.app.repositories;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrderByDateDeCreationDesc();

    List<User> findAllByRole(Role role);

    User findUserByRoleAndId(Role role , Long id);

    User findUserByRoleAndNom(Role role , String nom);

    User deleteByIdAndRole(Long id , Role role);

//    User findUserByNomOrPrenom(String nom);

    User findOneByVerificationToken(String token);
}
