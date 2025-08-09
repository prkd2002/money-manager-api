package com.rustytech.moneymanager.repository;

import com.rustytech.moneymanager.entity.CategoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends CrudRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByProfileId(Long profileId);
    Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profileId);
    List<CategoryEntity> findByTypeAndProfileId(String type, Long profileId);
    Boolean existsByNameAndProfileId(String name, Long profileId);
}
