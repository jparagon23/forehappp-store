package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Line;
import com.forehapp.store.productModule.domain.ports.out.ILineDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LineRepositoryAdapter implements ILineDao {

    private final ILineJpaRepository jpaRepository;
    private final IProductRepository productRepository;

    public LineRepositoryAdapter(ILineJpaRepository jpaRepository,
                                  IProductRepository productRepository) {
        this.jpaRepository = jpaRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Line> findById(Long id) {
        return jpaRepository.findByIdWithBrand(id);
    }

    @Override
    public List<Line> findAllByBrandId(Long brandId) {
        return jpaRepository.findByBrandId(brandId);
    }

    @Override
    public List<Line> findAllByBrandIdAndCategoryId(Long brandId, Long categoryId) {
        return jpaRepository.findByBrandIdAndCategoryId(brandId, categoryId);
    }

    @Override
    public Line save(Line line) {
        return jpaRepository.save(line);
    }

    @Override
    public void delete(Line line) {
        jpaRepository.delete(line);
    }

    @Override
    public boolean isUsedByProducts(Long lineId) {
        return productRepository.existsByLineId(lineId);
    }
}
