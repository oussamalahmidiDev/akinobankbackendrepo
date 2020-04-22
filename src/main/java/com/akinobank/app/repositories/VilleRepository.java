package com.akinobank.app.repositories;

import com.akinobank.app.models.Ville;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

@RepositoryRestController
public interface VilleRepository extends JpaRepository<Ville, Long> {}
