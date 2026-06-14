package com.nihit.craft_connect.service.category.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.ErrorConstants;
import com.nihit.craft_connect.constants.StringConstants;
import com.nihit.craft_connect.dto.category.CategoryDto;
import com.nihit.craft_connect.entity.Category;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.CategoryRepository;
import com.nihit.craft_connect.service.category.CategoryService;
import com.nihit.craft_connect.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CustomMessageSource customMessageSource;
    private final FileService fileService;
    private static final String FILE_LOCATION = "categories";
    private final ModelMapper modelMapper;

    @Override
    public CategoryDto saveOrUpdate(CategoryDto categoryDto){
        Category category;
        if (categoryDto.getId() != null){
            category = categoryRepository.findById(categoryDto.getId())
                    .orElseThrow(() -> new AppException(customMessageSource.get(ErrorConstants.ERROR_ALREADY_EXIST, categoryDto.getId())));
        }
        else {
            category = new Category();
        }
            category.setName(categoryDto.getName());
            category.setImagePath(fileService.uploadAttachment(categoryDto.getImage(), FILE_LOCATION));
        categoryRepository.save(category);
        CategoryDto categoryDtoSaved = new CategoryDto();
        categoryDtoSaved.setId(category.getId());
        categoryDtoSaved.setName(categoryDto.getName());
        return categoryDtoSaved;
    }

    @Override
    public List<CategoryDto> findAll() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .toList();
    }

    @Override
    public CategoryDto findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(customMessageSource.get(StringConstants.NOT_FOUND, id)));

        return modelMapper.map(category, CategoryDto.class);
    }

    @Override
    public void deleteById(Long id){
        categoryRepository.deleteById(id);
    }
}
