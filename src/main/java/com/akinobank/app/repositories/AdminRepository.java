package com.akinobank.app.repositories;

import com.akinobank.app.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

@RepositoryRestController
public interface AdminRepository  extends JpaRepository<Admin, Long> {
}
