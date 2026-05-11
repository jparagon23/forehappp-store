package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Line;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ILineJpaRepository extends JpaRepository<Line, Long> {

    @Query("SELECT l FROM Line l JOIN FETCH l.brand WHERE l.id = :id")
    Optional<Line> findByIdWithBrand(@Param("id") Long id);
}
