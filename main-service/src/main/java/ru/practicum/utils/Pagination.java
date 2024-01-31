package ru.practicum.utils;

import lombok.Getter;

@Getter
public class Pagination {
    private final Integer customSize = 1000;
    private Integer pageSize;
    private Integer pageStart;
    private Integer pagesAmount;

    public Pagination(Integer offset, Integer size) {
        pageSize = offset;
        pageStart = 1;
        pagesAmount = 0;
        if (size == null) {
            if (offset == 0) {
                pageSize = customSize;
                pageStart = 0;
            }
        } else {
            if (offset == 0) {
                pageSize = size;
                pageStart = 0;
            }

            pagesAmount = pageStart + 1;
            if ((offset < size) && (offset != 0)) {
                pagesAmount = size / offset + pageStart;
                if (size % offset != 0) {
                    pagesAmount++;
                }
            }
        }
    }
}
