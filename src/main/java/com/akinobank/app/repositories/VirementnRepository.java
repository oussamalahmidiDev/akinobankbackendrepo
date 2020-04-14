package com.akinobank.app.repositories;

import com.akinobank.app.models.Virement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VirementnRepository extends JpaRepository<Virement, UUID> {


}
