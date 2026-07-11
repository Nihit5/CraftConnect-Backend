package com.nihit.craft_connect.controller.product;

import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.product.ProductRequestPojo;
import com.nihit.craft_connect.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
public class ProductController extends BaseController {
    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<GlobalApiResponse> save(@Valid @ModelAttribute ProductRequestPojo productRequestPojo) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_CREATE, "Product"),
                productService.saveOrUpdate(productRequestPojo)));
    }

    @PutMapping("/update")
    public ResponseEntity<GlobalApiResponse> update(@Valid @ModelAttribute ProductRequestPojo productRequestPojo) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Product"),
                productService.saveOrUpdate(productRequestPojo)));
    }

    @GetMapping("/list")
    public ResponseEntity<GlobalApiResponse> getAll(
            @RequestParam(defaultValue = "0") Integer start,
            @RequestParam(defaultValue = "10") Integer length) {

        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Product"),
                productService.getAll(start, length)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalApiResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Product"),
                productService.getById(id)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GlobalApiResponse> deleteById(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_DELETE, "Product")));
    }

    @GetMapping("/list/category/{categoryId}")
    public ResponseEntity<GlobalApiResponse> getAll(@PathVariable Long categoryId) {

        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Product"),
                productService.getProductByCategory(categoryId)));
    }

    @GetMapping("/list/featured-product")
    public ResponseEntity<GlobalApiResponse> getFeaturedProducts() {

        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Product"),
                productService.getFeaturedProducts()));
    }

    @PutMapping("/feature/{productId}")
    public ResponseEntity<GlobalApiResponse> setFeatureProduct(@PathVariable Long productId) {
        productService.setFeaturedProduct(productId);
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Product")));
    }
}
