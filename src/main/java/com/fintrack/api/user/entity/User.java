
package com.fintrack.api.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String name;

    @Column(name = "email", nullable = false,
            unique = true, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false,
            length = 255)
    private String passwordHash;

    @Column(name = "criado_em",
            nullable = false,
            insertable = false,
            updatable = false)
    private OffsetDateTime createdAt;
}
