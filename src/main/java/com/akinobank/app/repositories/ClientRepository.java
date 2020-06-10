package com.akinobank.app.repositories;

import com.akinobank.app.models.Client;
import com.akinobank.app.models.Compte;
import com.akinobank.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;


//@RepositoryRestController
public interface ClientRepository extends JpaRepository<Client,Long> {

    Optional<Client> findByUser(User user);
    Client findClientByComptes(Compte compte);

    Collection<Client> findAllByAgenceId(Long id);
//    @RestResource(path = "agent/client/bynum")
//    Page<Client> findByNumeroTelephone(String numeroTelephone, Pageable pageable);
}
