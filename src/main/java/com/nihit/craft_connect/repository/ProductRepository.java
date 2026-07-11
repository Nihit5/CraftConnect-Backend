package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.dto.product.ProductPojo;
import com.nihit.craft_connect.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"user", "category"})
    List<Product> findAll();

    @Query(value = """
    SELECT
        id,
        name,
        description,
        image_path AS imagePath,
        price,
        quantity
    FROM product
    WHERE featured = true
    """, nativeQuery = true)
    List<ProductPojo> getAllFeaturedProducts();

    @Query("""
    SELECT p
    FROM Product p
    JOIN FETCH p.user
    JOIN FETCH p.category
    WHERE p.id = :id
    """)
    Optional<Product> findByIdWithUserAndCategory(Long id);

    List<Product> findByCategory_Id(Long categoryId);
}
