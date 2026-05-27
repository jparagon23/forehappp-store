package com.forehapp.store.general.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PagedResponse(Page<T> springPage) {
        this.content = springPage.getContent();
        this.page = springPage.getNumber();
        this.size = springPage.getSize();
        this.totalElements = springPage.getTotalElements();
        this.totalPages = springPage.getTotalPages();
        this.hasNext = springPage.hasNext();
        this.hasPrevious = springPage.hasPrevious();
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasNext() { return hasNext; }
    public boolean isHasPrevious() { return hasPrevious; }
}
