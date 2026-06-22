package com.restaurant.infrastructure.persistence.repository;

import com.restaurant.domain.model.CuisineType;
import com.restaurant.domain.model.DietaryOption;
import com.restaurant.infrastructure.persistence.entity.RestaurantEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JpaRestaurantRepository extends JpaRepository<RestaurantEntity, Long> {

    List<RestaurantEntity> findByNameContainingIgnoreCase(String name);

    List<RestaurantEntity> findByCityIgnoreCase(String city);

    List<RestaurantEntity> findByProvinceIgnoreCase(String province);

    List<RestaurantEntity> findByCuisineType(CuisineType cuisineType);

    List<RestaurantEntity> findByAveragePriceLessThanEqual(BigDecimal maxPrice);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RestaurantEntity r WHERE r.id = :id")
    Optional<RestaurantEntity> findByIdUpdate(Long id);

    boolean existsByEmail(String email);

    @Query("""
            SELECT DISTINCT r FROM RestaurantEntity r
            LEFT JOIN r.dietaryOptions d
            WHERE (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:city IS NULL OR LOWER(r.city) = LOWER(:city))
              AND (:province IS NULL OR LOWER(r.province) = LOWER(:province))
              AND (:cuisineType IS NULL OR r.cuisineType = :cuisineType)
              AND (:dietaryOption IS NULL OR d = :dietaryOption)
              AND (:maxPrice IS NULL OR r.averagePrice <= :maxPrice)
            """)
    List<RestaurantEntity> search(@Param("name") String name,
                                  @Param("city") String city,
                                  @Param("province") String province,
                                  @Param("cuisineType") CuisineType cuisineType,
                                  @Param("dietaryOption") DietaryOption dietaryOption,
                                  @Param("maxPrice") BigDecimal maxPrice);
}
