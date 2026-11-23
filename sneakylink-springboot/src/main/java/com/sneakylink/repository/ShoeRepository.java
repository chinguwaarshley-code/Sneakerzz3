package com.sneakylink.repository;

import com.sneakylink.model.Shoe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShoeRepository extends JpaRepository<Shoe, Long>, JpaSpecificationExecutor<Shoe> {

    Optional<Shoe> findBySkuIgnoreCase(String sku);

    @Query("SELECT DISTINCT s.colorway FROM Shoe s ORDER BY s.colorway ASC")
    List<String> findDistinctColorways();

    @Query("SELECT DISTINCT s.primaryColor FROM Shoe s ORDER BY s.primaryColor ASC")
    List<String> findDistinctPrimaryColors();

    @Query("SELECT DISTINCT s.sizeRange FROM Shoe s ORDER BY s.sizeRange ASC")
    List<String> findDistinctSizeRanges();

    @Query("SELECT DISTINCT s.material FROM Shoe s ORDER BY s.material ASC")
    List<String> findDistinctMaterials();

    @Query("SELECT DISTINCT s.brand FROM Shoe s ORDER BY s.brand ASC")
    List<String> findDistinctBrands();

    @Query("SELECT DISTINCT s.category FROM Shoe s ORDER BY s.category ASC")
    List<String> findDistinctCategories();

    long countByStockQuantityLessThanEqual(Integer threshold);

    long countByStockQuantityEquals(Integer quantity);
}
