package com.bookstore.controller;

import com.bookstore.dto.BookRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookRequest validRequest;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        validRequest = new BookRequest("Clean Code", "Robert C. Martin",
                "978-0132350884", new BigDecimal("34.99"), 50, "Technology");

        bookResponse = new BookResponse(1L, "Clean Code", "Robert C. Martin",
                "978-0132350884", new BigDecimal("34.99"), 50, "Technology",
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createBook_validRequest_returns201() throws Exception {
        when(bookService.createBook(any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("978-0132350884"));
    }

    @Test
    void createBook_invalidRequest_returns400() throws Exception {
        BookRequest invalid = new BookRequest("", "", "",
                new BigDecimal("-1"), -5, null);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAllBooks_returns200WithList() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void getBookById_exists_returns200() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(bookResponse);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getBookById_notFound_returns404() throws Exception {
        when(bookService.getBookById(99L))
                .thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(get("/api/v1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Book not found with id: 99"));
    }

    @Test
    void updateBook_validRequest_returns200() throws Exception {
        when(bookService.updateBook(eq(1L), any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void deleteBook_exists_returns204() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBook_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Book not found with id: 99"))
                .when(bookService).deleteBook(99L);

        mockMvc.perform(delete("/api/v1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getBooksByCategory_returns200() throws Exception {
        when(bookService.getBooksByCategory("Technology")).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/v1/books/category/Technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Technology"));
    }

    @Test
    void searchByAuthor_returns200() throws Exception {
        when(bookService.searchBooksByAuthor("Martin")).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/v1/books/search").param("author", "Martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("Robert C. Martin"));
    }
}
