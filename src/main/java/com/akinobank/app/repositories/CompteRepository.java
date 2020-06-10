package com.akinobank.app.repositories;

import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

//@RepositoryRestController
public interface CompteRepository  extends JpaRepository<Compte, String> {

    Collection<Compte> findAllByClientId(Long id);

    Optional<Compte> findByNumeroCompteAndClient(String numeroCompte, Client client);

//    Compte findByClient_Agent_AgenceAndNumeroCompte(String numeroCompte);

    Optional<Compte> findByClient_Agent_AgenceAndNumeroCompte(Agence agence, String id);

    Compte findByNumeroCompteContaining(String id);

    Compte findCompteByNumeroCompteIsContaining(String numeroCompte);

    Optional<Compte> findByClient_Agent_AgenceAndNumeroCompteContaining(Agence agence, String id); // that s what we need because numeroCompte is hidden we saw just the last 4 numbers
    Optional<Compte> findByClient_AgenceAndNumeroCompteContaining(Agence agence, String id);
    Optional<Compte> findByClientAndClient_AgenceAndNumeroCompteContaining(Client client ,Agence agence, String id);


    Collection<Compte> findAllByClientIdAndNumeroCompte(Long id,String numero_compte);

//    Optional<Compte> findByClient_Agent_AgenceAndNumeroCompte(Agence agence, String id);
//    Page<Compte> findAllByClientId(Long id, Pageable pageable);



}
