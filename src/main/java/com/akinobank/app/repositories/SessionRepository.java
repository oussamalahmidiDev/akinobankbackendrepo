package com.akinobank.app.repositories;

import com.akinobank.app.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
}
