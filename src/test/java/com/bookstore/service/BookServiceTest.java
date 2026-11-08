package com.bookstore.service;

import com.bookstore.domain.Book;
import com.bookstore.dto.BookRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.exception.DuplicateIsbnException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookRequest request;
    private BookResponse response;

    @BeforeEach
    void setUp() {
        request = new BookRequest("Clean Code", "Robert C. Martin",
                "978-0132350884", new BigDecimal("34.99"), 50, "Technology");

        book = new Book("Clean Code", "Robert C. Martin",
                "978-0132350884", new BigDecimal("34.99"), 50, "Technology");
        book.setId(1L);

        response = new BookResponse(1L, "Clean Code", "Robert C. Martin",
                "978-0132350884", new BigDecimal("34.99"), 50, "Technology",
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createBook_happyPath_returnsResponse() {
        when(bookRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(request)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponse result = bookService.createBook(request);

        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("978-0132350884");
        verify(bookRepository).save(book);
    }

    @Test
    void createBook_duplicateIsbn_throwsException() {
        when(bookRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(DuplicateIsbnException.class)
                .hasMessageContaining("978-0132350884");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBookById_exists_returnsResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponse result = bookService.getBookById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookById_notFound_throwsResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllBooks_returnsList() {
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toResponseList(List.of(book))).thenReturn(List.of(response));

        List<BookResponse> result = bookService.getAllBooks();

        assertThat(result).hasSize(1);
    }

    @Test
    void updateBook_happyPath_updatesAndReturns() {
        // Same ISBN — no duplicate check triggered
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(response);

        BookResponse result = bookService.updateBook(1L, request);

        assertThat(result).isNotNull();
        verify(bookRepository).save(book);
    }

    @Test
    void deleteBook_exists_deletesSuccessfully() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_notFound_throwsResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookRepository, never()).delete(any());
    }
}
