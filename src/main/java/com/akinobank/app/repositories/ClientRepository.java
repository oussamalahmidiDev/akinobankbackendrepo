package com.akinobank.app.repositories;

import com.akinobank.app.models.Client;
import com.akinobank.app.models.Compte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.RepositoryRestController;

import java.util.Collection;
import java.util.List;


@RepositoryRestController
public interface ClientRepository extends JpaRepository<Client,Long> {

    Client findClientByUserId(Long id);
    Client findClientByComptes(Compte compte);

    Collection<Client> findAllByAgenceId(Long id);
//    @RestResource(path = "agent/client/bynum")
//    Page<Client> findByNumeroTelephone(String numeroTelephone, Pageable pageable);
}
