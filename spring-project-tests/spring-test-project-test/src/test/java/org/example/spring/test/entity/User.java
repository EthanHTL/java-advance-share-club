package org.example.spring.test.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "tuser")
@Entity
@Data
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;
}