package com.akinobank.app.repositories;

import com.akinobank.app.models.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRedisRepository extends CrudRepository<Session, String> {

    Optional<Session> findByRefreshToken(String token);

    Session findByIdAndUserId(String id, Long user);

    List<Session> findAllByUserId(Long user);

}
