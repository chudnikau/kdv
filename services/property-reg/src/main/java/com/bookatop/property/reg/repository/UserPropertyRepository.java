package com.bookatop.property.reg.repository;

import com.bookatop.property.reg.entity.UserPropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPropertyRepository extends JpaRepository<UserPropertyEntity, Long> {

    List<UserPropertyEntity> findAllByRefUserIdOrderByPropertyIdDesc(Long refUserId);

}
