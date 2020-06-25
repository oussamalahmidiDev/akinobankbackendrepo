package com.akinobank.app.repositories;

import com.akinobank.app.enumerations.Role;
import com.akinobank.app.models.Agence;
import com.akinobank.app.models.Client;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


//@RepositoryRestController
public interface ClientRepository extends JpaRepository<Client,Long> {

    Optional<Client> findByUser(User user);
    Client findClientByComptes(Compte compte);
    Optional<Client> findById(Long id);
    Optional<Client> findByIdAndAgence(Long id , Agence agence);

    Collection<Client> findAllByAgenceId(Long id);
    List<Client> findAllByAgence(Agence agence);
//    @RestResource(path = "agent/client/bynum")
//    Page<Client> findByNumeroTelephone(String numeroTelephone, Pageable pageable);
}
