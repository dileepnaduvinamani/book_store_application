package com.bookstore.mapper;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookRequest;
import com.bookstore.dto.BookResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookMapper {

    public Book toEntity(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        book.setCategory(request.getCategory());
        return book;
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getCategory(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    public List<BookResponse> toResponseList(List<Book> books) {
        return books.stream()
                .map(this::toResponse)
                .toList();
    }
}
