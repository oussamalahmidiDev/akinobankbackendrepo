package com.akinobank.app.repositories;

import com.akinobank.app.models.Client;
import com.akinobank.app.models.Virement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//@RepositoryRestController
public interface VirementRepository extends JpaRepository<Virement, Long> {

    Optional<Virement> findByIdAndAndCompte_Client(Long id, Client client);
//    List<Virement> findAllByCompte_Client(Client client);
}
