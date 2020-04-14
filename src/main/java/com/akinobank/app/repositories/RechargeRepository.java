package com.akinobank.app.repositories;

import com.akinobank.app.models.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface RechargeRepository  extends JpaRepository<Recharge, Long> {
}
