package com.akinobank.app.repositories;

import com.akinobank.app.models.Agence;
import org.springframework.data.jpa.repository.JpaRepository;

//@RepositoryRestController
public interface AgenceRepository extends JpaRepository<Agence, Long> {
}
