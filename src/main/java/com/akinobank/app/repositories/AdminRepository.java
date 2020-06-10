package com.akinobank.app.repositories;

import com.akinobank.app.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository  extends JpaRepository<Admin, Long> {
}
