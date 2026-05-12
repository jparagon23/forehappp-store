package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.Line;

import java.util.List;
import java.util.Optional;

public interface ILineDao {
    Optional<Line> findById(Long id);
    List<Line> findAllByBrandId(Long brandId);
    Line save(Line line);
}
