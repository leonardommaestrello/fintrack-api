
package com.fintrack.api.category.entity;

import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Column(name = "nome", nullable = false, length = 80)
    private String name;

    @Column(name = "tipo", nullable = false, length = 10)
    private TransactionType type;

    @Column(name = "cor", nullable = false, length = 7)
    private String color = "#10B981";

    @Column(name = "criado_em",
            nullable = false,
            insertable = false,
            updatable = false)
    private OffsetDateTime createdAt;
}
