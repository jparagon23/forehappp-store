package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IAttributeValueJpaRepository extends JpaRepository<AttributeValue, Long> {

    @Query("SELECT av FROM AttributeValue av JOIN FETCH av.attribute WHERE av.id IN :ids")
    List<AttributeValue> findByIdInWithAttribute(@Param("ids") List<Long> ids);

    @Query("SELECT av FROM AttributeValue av JOIN FETCH av.attribute WHERE av.attribute.id IN :attributeIds")
    List<AttributeValue> findByAttributeIdIn(@Param("attributeIds") List<Long> attributeIds);
}
