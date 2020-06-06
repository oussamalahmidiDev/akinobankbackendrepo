package com.akinobank.app.models;

import com.akinobank.app.utilities.VerificationTokenGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Session {

    @Id
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

    @PrePersist
    void beforeInsert() {
        id = UUID.randomUUID().toString().replace("-","");
        refreshToken = VerificationTokenGenerator.generateVerificationToken(); // this is temporary.
        authorized = false;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
