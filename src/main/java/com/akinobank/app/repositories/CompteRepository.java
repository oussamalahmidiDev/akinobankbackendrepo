package com.akinobank.app.repositories;

import com.akinobank.app.models.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@RepositoryRestController
public interface CompteRepository  extends JpaRepository<Compte, String> {
}
