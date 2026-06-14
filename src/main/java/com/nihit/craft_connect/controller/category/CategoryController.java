package com.nihit.craft_connect.controller.category;

import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.category.CategoryDto;
import com.nihit.craft_connect.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
public class CategoryController extends BaseController {
    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<GlobalApiResponse> save(@Valid @ModelAttribute CategoryDto categoryDto) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_CREATE, "Category"),
                categoryService.saveOrUpdate(categoryDto)));
    }

    @PutMapping("/update")
    public ResponseEntity<GlobalApiResponse> update(@Valid @ModelAttribute CategoryDto categoryDto) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_UPDATE,"Category"),
                categoryService.saveOrUpdate(categoryDto)));
    }

    @GetMapping("/list")
    public ResponseEntity<GlobalApiResponse> list() {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE,"Category"),
                categoryService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalApiResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE,"Category"),
                categoryService.findById(id)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GlobalApiResponse> deleteById(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_DELETE,"Category")));
    }
}
