package com.akinobank.app.repositories;

import com.akinobank.app.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ClientRepository extends JpaRepository<Client,Integer > {
}
