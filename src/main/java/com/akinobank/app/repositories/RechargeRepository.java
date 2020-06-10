package com.akinobank.app.repositories;

import com.akinobank.app.models.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;

//@RepositoryRestController
public interface RechargeRepository  extends JpaRepository<Recharge, Long> {
}
