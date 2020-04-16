package com.akinobank.app.repositories;

import com.akinobank.app.models.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

@RepositoryRestController
public interface AgentRepository extends JpaRepository<Agent,Long> {

}
