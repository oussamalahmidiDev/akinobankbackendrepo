package com.akinobank.app.repositories;

import com.akinobank.app.models.Agent;
import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

import java.util.Optional;

@RepositoryRestController
public interface AgentRepository extends JpaRepository<Agent,Long> {
    Optional<Agent> findByUser(User user);
}
