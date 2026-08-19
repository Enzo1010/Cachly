package br.com.cachly.backend.comum.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * Superclasse que centraliza os campos de auditoria para entidades
 * administrativas (Categoria, Questao, Alternativa).
 *
 * Os campos de data (criadoEm, atualizadoEm) e autoria (criadoPor, atualizadoPor)
 * são preenchidos automaticamente pelo Spring Data JPA Auditing,
 * eliminando a necessidade de @PrePersist/@PreUpdate manuais.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class EntidadeAuditavel {

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @CreatedBy
    @Column(name = "criado_por", updatable = false)
    private Long criadoPor;

    @LastModifiedBy
    @Column(name = "atualizado_por")
    private Long atualizadoPor;
}
