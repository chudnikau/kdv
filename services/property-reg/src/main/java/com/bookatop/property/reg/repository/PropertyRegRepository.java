package com.bookatop.property.reg.repository;

import com.bookatop.property.reg.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRegRepository extends JpaRepository<PropertyEntity, Long> {

    List<PropertyEntity> findAllByOrderByIdDesc();

}
