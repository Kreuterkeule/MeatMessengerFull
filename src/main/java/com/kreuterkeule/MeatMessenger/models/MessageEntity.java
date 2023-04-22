package com.kreuterkeule.MeatMessenger.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Getter
@Setter
@Data
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @CreationTimestamp
    @Column(name = "CreateDate", updatable = false)
    private Timestamp CreatedTime;
    private String text;
    @Column(name = "valueFrom")
    private String from;
    @Column(name = "valueTo")
    private String to;
    private Boolean isUpdated;

}
