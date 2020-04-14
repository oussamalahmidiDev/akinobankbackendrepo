package com.akinobank.app.repositories;

import com.akinobank.app.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
public interface AdminRepository  extends JpaRepository<Admin, Integer> {
}
