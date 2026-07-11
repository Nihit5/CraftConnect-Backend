package com.nihit.craft_connect.service.product;

import com.nihit.craft_connect.dto.PaginationResponsePojo;
import com.nihit.craft_connect.dto.product.ProductPojo;
import com.nihit.craft_connect.dto.product.ProductRequestPojo;
import com.nihit.craft_connect.dto.product.ProductResponsePojo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {
    ProductResponsePojo saveOrUpdate(ProductRequestPojo productRequestPojo);
    List<ProductResponsePojo> getAll();
    ProductResponsePojo getById(Long id);
    void delete(Long id);
    List<ProductPojo> getProductByCategory(Long categoryId);
    List<ProductPojo> getFeaturedProducts();
    void setFeaturedProduct(Long productId);
    List<ProductResponsePojo> getProductsByUserId(Long userId);
}
