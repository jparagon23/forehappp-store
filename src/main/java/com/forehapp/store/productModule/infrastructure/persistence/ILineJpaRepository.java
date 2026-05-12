package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Line;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ILineJpaRepository extends JpaRepository<Line, Long> {

    @Query("SELECT l FROM Line l JOIN FETCH l.brand WHERE l.id = :id")
    Optional<Line> findByIdWithBrand(@Param("id") Long id);

    @Query("SELECT l FROM Line l WHERE l.brand.id = :brandId")
    List<Line> findByBrandId(@Param("brandId") Long brandId);
}
