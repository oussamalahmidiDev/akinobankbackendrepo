package com.akinobank.app.repositories;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByRoleIsNotOrderByDateDeCreationDesc(Role role);

    List<User> findAllByRole(Role role);
    List<User> findAllByRoleIsNot(Role role);
    User findByPassword(String password);

    User findUserByRoleAndId(Role role , Long id);

    User deleteByIdAndRole(Long id , Role role);

//    User findUserByNomOrPrenom(String nom);

    User findOneByVerificationToken(String token);

    List<User> findUserByRoleAndNomAndAgent_Agence(Role role, String clientName, Agence agence);

//    Optional<User> findByEmail(String email);

    User findByEmail(String email);

    Optional<User> findByRefreshToken(String token);

    Optional<User> findByIdAndRoleIsNot(Long id, Role role);

    List<User> findAllByRoleAndArchived(Role role , Boolean archived);

    Long countByRole(Role role);
    Long countByRoleIsNot(Role role);

}
