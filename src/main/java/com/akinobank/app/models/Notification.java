package com.akinobank.app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class Notification {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenu;

    @CreationTimestamp
    private Date timestamp;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    @JsonIgnoreProperties({ "receiver", "notification" })
    @ToString.Exclude
    private List<UserNotification> UserNotification;
}

