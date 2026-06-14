package com.nihit.craft_connect.service.category;

import com.nihit.craft_connect.dto.category.CategoryDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    CategoryDto saveOrUpdate(CategoryDto categoryDto);
    List<CategoryDto> findAll();
    CategoryDto findById(Long id);
    void deleteById(Long id);
}
