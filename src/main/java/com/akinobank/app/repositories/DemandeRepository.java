package com.akinobank.app.repositories;

import com.akinobank.app.models.Client;
import com.akinobank.app.models.Demande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeRepository extends JpaRepository<Demande, Long> {
    void deleteAllByClient(Client client);

    Demande findByClient(Client client);
}
