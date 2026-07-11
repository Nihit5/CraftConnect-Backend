package com.nihit.craft_connect.service.product.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.StringConstants;
import com.nihit.craft_connect.dto.PaginationResponsePojo;
import com.nihit.craft_connect.dto.product.ProductPojo;
import com.nihit.craft_connect.dto.product.ProductRequestPojo;
import com.nihit.craft_connect.dto.product.ProductResponsePojo;
import com.nihit.craft_connect.entity.Category;
import com.nihit.craft_connect.entity.Product;
import com.nihit.craft_connect.entity.User;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.CategoryRepository;
import com.nihit.craft_connect.repository.ProductRepository;
import com.nihit.craft_connect.repository.UserRepository;
import com.nihit.craft_connect.service.file.FileService;
import com.nihit.craft_connect.service.product.ProductService;
import com.nihit.craft_connect.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CustomMessageSource customMessageSource;
    private final UserRepository userRepository;
    private final UserDetailConfig userDetailConfig;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    @Override
    public ProductResponsePojo saveOrUpdate(ProductRequestPojo productRequestPojo) {

        Product product;
        if (productRequestPojo.getId() != null) {
            product = productRepository.findById(productRequestPojo.getId())
                    .orElseThrow(() -> new AppException(
                            customMessageSource.get(
                                    StringConstants.NOT_FOUND,
                                    customMessageSource.get(StringConstants.PRODUCT)
                            )
                    ));

        } else {
            product = new Product();
            product.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            product.setCreatedBy(userDetailConfig.getLoggedInUserId());
            product.setFeatured(false);
        }

        User user = userRepository.findById(userDetailConfig.getLoggedInUserId())
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                StringConstants.USER
                        )
                ));

        Category category = categoryRepository.findById(productRequestPojo.getCategoryId())
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                        "Category"
                        )
                ));

        product.setName(productRequestPojo.getName());

        product.setDescription(productRequestPojo.getDescription());

        product.setPrice(productRequestPojo.getPrice());

        product.setQuantity(productRequestPojo.getQuantity());

        product.setFeatured(productRequestPojo.getFeatured());

        if (productRequestPojo.getImage() != null &&
                !productRequestPojo.getImage().isEmpty()) {

            product.setImagePath(
                    fileService.uploadAttachment(productRequestPojo.getImage())
            );
        }

        product.setUser(user);

        product.setCategory(category);

        product.setModifiedDate(new Timestamp(System.currentTimeMillis()));

        product.setModifiedBy(userDetailConfig.getLoggedInUserId());

        productRepository.save(product);

        ProductResponsePojo response = new ProductResponsePojo();

        response.setId(product.getId());

        response.setName(product.getName());

        response.setDescription(product.getDescription());

        response.setImagePath(
                fileService.extractFileName(product.getImagePath())
        );

        response.setPrice(product.getPrice());

        response.setQuantity(product.getQuantity());

        response.setFeatured(product.getFeatured());

        response.setUserId(product.getUser().getId());

        response.setUserName(
                product.getUser().getFirstName() + " " + product.getUser().getLastName()
        );

        response.setCategoryId(product.getCategory().getId());

        response.setCategoryName(product.getCategory().getName());

        return response;
    }

    @Override
    public List<ProductResponsePojo> getAll() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(this::map)
                .toList();

    }

    @Override
    public List<ProductResponsePojo> getProductsByUserId(Long userId) {
        return productRepository.findProductsByUserId(userId);


    }

    @Override
    public List<ProductPojo> getProductByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategory_Id(categoryId);

        return products.stream()
                .map(this::mapToPojo)
                .toList();
    }

    @Override
    public ProductResponsePojo getById(Long id) {

        Product product = productRepository.findByIdWithUserAndCategory(id)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                StringConstants.PRODUCT
                        )
                ));

        return map(product);
    }
    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                StringConstants.PRODUCT
                        )
                ));
        productRepository.delete(product);
    }

    @Override
    public List<ProductPojo> getFeaturedProducts() {
        return productRepository.getAllFeaturedProducts();

    }

    public void setFeaturedProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(
                customMessageSource.get(
                        StringConstants.NOT_FOUND,
                        StringConstants.PRODUCT
                )
        ));
        product.setFeatured(!Boolean.TRUE.equals(product.getFeatured()));
        productRepository.save(product);
    }

    private ProductResponsePojo map(Product product) {
        ProductResponsePojo response = new ProductResponsePojo();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setFeatured(product.getFeatured());

        response.setImagePath(fileService.extractFileName(product.getImagePath()));

        response.setUserId(product.getUser().getId());
        response.setUserName(product.getUser().getFirstName() + " " + product.getUser().getLastName());

        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());

        return response;
    }

    private ProductPojo mapToPojo(Product product) {
        ProductPojo response = new ProductPojo();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());

        return response;
    }
}
