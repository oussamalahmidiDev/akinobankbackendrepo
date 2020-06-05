package com.akinobank.app.models;

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

    @PrePersist
    void beforeInsert() {
        id = UUID.randomUUID().toString().replace("-","");
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
