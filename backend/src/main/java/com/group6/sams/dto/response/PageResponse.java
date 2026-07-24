package com.group6.sams.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Paging envelope returned by every list endpoint.
 *
 * Spring's Page serializes with an unstable internal structure and drags the
 * whole Pageable into the payload, so we expose our own flat shape that the React
 * pagination component can rely on.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    /** Maps a Page of entities to a PageResponse of DTOs in one step. */
    public static <E, D> PageResponse<D> from(Page<E> source, Function<E, D> mapper) {
        return PageResponse.<D>builder()
                .content(source.getContent().stream().map(mapper).toList())
                .page(source.getNumber())
                .size(source.getSize())
                .totalElements(source.getTotalElements())
                .totalPages(source.getTotalPages())
                .first(source.isFirst())
                .last(source.isLast())
                .build();
    }
}
