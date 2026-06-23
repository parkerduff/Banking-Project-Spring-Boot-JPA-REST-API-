package com.sr_banking.banking_project.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Stable pagination envelope for collection endpoints (ABS-MAS Playbook: consistent data contracts). */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {

    public static <S, T> PagedResponse<T> from(Page<S> source, List<T> content) {
        return new PagedResponse<>(
                content,
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isLast());
    }
}
