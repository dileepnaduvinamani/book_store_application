package com.bookstore.config;

import com.bookstore.domain.Book;
import com.bookstore.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return; // Already seeded — skip
        }

        bookRepository.saveAll(List.of(
                new Book("Clean Code", "Robert C. Martin",
                        "978-0132350884", new BigDecimal("34.99"), 50, "Technology"),
                new Book("The Pragmatic Programmer", "David Thomas",
                        "978-0135957059", new BigDecimal("39.99"), 40, "Technology"),
                new Book("Design Patterns", "Gang of Four",
                        "978-0201633610", new BigDecimal("44.99"), 30, "Technology"),
                new Book("To Kill a Mockingbird", "Harper Lee",
                        "978-0061935466", new BigDecimal("12.99"), 100, "Fiction"),
                new Book("1984", "George Orwell",
                        "978-0451524935", new BigDecimal("10.99"), 120, "Fiction"),
                new Book("A Brief History of Time", "Stephen Hawking",
                        "978-0553380163", new BigDecimal("14.99"), 60, "Science"),
                new Book("The Selfish Gene", "Richard Dawkins",
                        "978-0198788607", new BigDecimal("16.99"), 45, "Science")
        ));
    }
}
