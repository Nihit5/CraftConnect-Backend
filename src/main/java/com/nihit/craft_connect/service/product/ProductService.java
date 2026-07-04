package com.nihit.craft_connect.service.product;

import com.nihit.craft_connect.dto.product.ProductRequestPojo;
import com.nihit.craft_connect.dto.product.ProductResponsePojo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {
    ProductResponsePojo saveOrUpdate(ProductRequestPojo productRequestPojo);
    List<ProductResponsePojo> getAll();
    ProductResponsePojo getById(Long id);
    void delete(Long id);
}
