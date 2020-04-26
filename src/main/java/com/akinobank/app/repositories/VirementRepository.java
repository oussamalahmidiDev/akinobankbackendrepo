package com.akinobank.app.repositories;

import com.akinobank.app.models.Client;
import com.akinobank.app.models.Virement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

import java.util.Optional;

@RepositoryRestController
public interface VirementRepository extends JpaRepository<Virement, Long> {

    Optional<Virement> findByIdAndAndCompte_Client(Long id, Client client);
//    List<Virement> findAllByCompte_Client(Client client);
}
