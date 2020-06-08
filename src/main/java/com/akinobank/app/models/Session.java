package com.akinobank.app.models;

import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@Log4j2
@RedisHash("Sessions")
public class Session {

    @Id
    @Indexed
    private String id;

    @CreationTimestamp
    private Date timestamp;

    private String browser;

    private String operatingSystem;

    private String ip;

    private String ville;
    private String pays;

    private Boolean authorized;

    @JsonIgnore
    private String refreshToken;

    @Indexed
    private Long userId;

    @PrePersist
    void beforeInsert() {
        id = UUID.randomUUID().toString().replace("-","");
        refreshToken = VerificationTokenGenerator.generateVerificationToken(); // this is temporary.
        authorized = false;
    }

    public Session init () {
        setId(UUID.randomUUID().toString().replace("-",""));
        setRefreshToken(VerificationTokenGenerator.generateVerificationToken());  // this is temporary.
        setAuthorized(false);
        setTimestamp(new Date());
        return this;
    }
}
