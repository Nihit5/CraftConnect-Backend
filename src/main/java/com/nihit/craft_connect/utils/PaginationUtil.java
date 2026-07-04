package com.nihit.craft_connect.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    private PaginationUtil() {
    }

    public static Pageable getPageable(Integer start, Integer length) {

        if (start == null || start < 0) {
            start = 0;
        }

        if (length == null || length <= 0) {
            length = 10;
        }

        int page = start / length;

        return PageRequest.of(page, length);
    }

    public static Pageable getPageable(Integer start,
                                       Integer length,
                                       String sortBy,
                                       Sort.Direction direction) {

        if (start == null || start < 0) {
            start = 0;
        }

        if (length == null || length <= 0) {
            length = 10;
        }

        int page = start / length;

        return PageRequest.of(page, length, Sort.by(direction, sortBy));
    }
}
