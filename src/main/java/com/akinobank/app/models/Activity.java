package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String evenement;

    private String category;

    @CreationTimestamp
    private Date timestamp;

    @ManyToOne()
    @JsonIgnoreProperties({ "admin","client","agent","email","emailConfirmed","archived" })
    private User user;

}
