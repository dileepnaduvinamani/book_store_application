package com.bookstore.service;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.exception.DuplicateIsbnException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        if (bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new DuplicateIsbnException(request.getIsbn());
        }
        Book saved = bookRepository.save(bookMapper.toEntity(request));
        return bookMapper.toResponse(saved);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
        return bookMapper.toResponse(book);
    }

    public List<BookResponse> getAllBooks() {
        return bookMapper.toResponseList(bookRepository.findAll());
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));

        // If ISBN is changing, check it doesn't collide with another book
        if (!existing.getIsbn().equals(request.getIsbn()) &&
                bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new DuplicateIsbnException(request.getIsbn());
        }

        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setIsbn(request.getIsbn());
        existing.setPrice(request.getPrice());
        existing.setStockQuantity(request.getStockQuantity());
        existing.setCategory(request.getCategory());

        return bookMapper.toResponse(bookRepository.save(existing));
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
        bookRepository.delete(book);
    }

    public List<BookResponse> getBooksByCategory(String category) {
        return bookMapper.toResponseList(bookRepository.findByCategory(category));
    }

    public List<BookResponse> searchBooksByAuthor(String author) {
        return bookMapper.toResponseList(
                bookRepository.findByAuthorContainingIgnoreCase(author));
    }
}
