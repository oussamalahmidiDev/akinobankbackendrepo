package com.akinobank.app.repositories;

import com.akinobank.app.models.Compte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@RepositoryRestController
public interface CompteRepository  extends JpaRepository<Compte, String> {

    Collection<Compte> findAllByClientId(Long id);
//    Page<Compte> findAllByClientId(Long id, Pageable pageable);



}
