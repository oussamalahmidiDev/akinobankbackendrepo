package com.akinobank.app.repositories;

import com.akinobank.app.models.Ville;
import org.springframework.data.jpa.repository.JpaRepository;

//@RepositoryRestController
public interface VilleRepository extends JpaRepository<Ville, Long> {}
