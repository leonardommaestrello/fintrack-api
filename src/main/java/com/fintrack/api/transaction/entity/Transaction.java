
package com.fintrack.api.transaction.entity;

import com.fintrack.api.category.entity.Category;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transacao")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Category category;

    @Column(name = "descricao", nullable = false, length = 200)
    private String description;

    @Column(name = "valor", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "tipo", nullable = false, length = 10)
    private TransactionType type;

    @Column(name = "data_transacao", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "criado_em",
            nullable = false,
            insertable = false,
            updatable = false)
    private OffsetDateTime createdAt;
}
