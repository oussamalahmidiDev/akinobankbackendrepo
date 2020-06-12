package com.akinobank.app.repositories;

import com.akinobank.app.enumerations.VirementStatus;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.Virement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//@RepositoryRestController
public interface VirementRepository extends JpaRepository<Virement, Long> {

    Optional<Virement> findByIdAndAndCompte_Client(Long id, Client client);

    Optional<Virement> findByIdAndDestCompte_Client(Long id, Client client);

//    Optional<Virement> findByDestCompteAnd

    List<Virement> findAllByDestCompteAndStatutIsNot(Compte compte, VirementStatus status);

    List<Virement> findAllByCompte(Compte compte);
//    List<Virement> findAllByCompte_Client(Client client);
}
