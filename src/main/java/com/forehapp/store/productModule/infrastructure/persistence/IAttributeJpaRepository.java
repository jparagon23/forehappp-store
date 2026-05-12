package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAttributeJpaRepository extends JpaRepository<Attribute, Long> {
}
