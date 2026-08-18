package com.retailcloud.ems.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Paginated response envelope. Every list endpoint returns this shape so
 * clients always know which page they are on and how many pages exist.
 *
 * @param content       items of the current page
 * @param page          zero-based index of the current page
 * @param size          number of items per page
 * @param totalElements total number of items across all pages
 * @param totalPages    total number of pages
 * @param first         true if this is the first page
 * @param last          true if this is the last page
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /** Builds a PageResponse directly from a Spring Data page. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    /** Builds a PageResponse from a page of entities, mapping each item to a DTO. */
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
