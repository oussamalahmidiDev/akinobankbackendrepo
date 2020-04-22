package com.akinobank.app.repositories;

import com.akinobank.app.models.Virement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

import java.util.UUID;

@RepositoryRestController
public interface VirementRepository extends JpaRepository<Virement, String> {


}
