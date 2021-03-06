package com.akinobank.app.repositories;

import com.akinobank.app.models.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.stereotype.Repository;

@RepositoryRestController
public interface RechargeRepository  extends JpaRepository<Recharge, Long> {
}
