# Bookstore API — Test Results

**Application:** `bookstore-api` (Spring Boot 3.2.5)  
**Database:** PostgreSQL 18 — `bookstoredb` @ `localhost:5432`  
**Base URL:** `http://localhost:8081/api/v1/books`  
**Test Date:** 2026-11-02  
**Java Version:** JDK 18.0.2.1  
**Server Status:** ✅ Running on port 8081

---

## Controller Endpoints Under Test

| # | Method | Path | Description |
|---|--------|------|-------------|
| 1 | `POST` | `/api/v1/books` | Create a new book |
| 2 | `GET` | `/api/v1/books` | Get all books |
| 3 | `GET` | `/api/v1/books/{id}` | Get a book by ID |
| 4 | `PUT` | `/api/v1/books/{id}` | Update an existing book |
| 5 | `GET` | `/api/v1/books/category/{category}` | Get books by category |
| 6 | `GET` | `/api/v1/books/search?author=` | Search books by author name |
| 7 | `DELETE` | `/api/v1/books/{id}` | Delete a book by ID |

---

## Test Summary

| # | Method | Endpoint | Scenario | Expected | Actual | Result |
|---|--------|----------|----------|----------|--------|--------|
| 1 | POST | `/api/v1/books` | Create valid book | 201 | 201 | ✅ PASS |
| 2 | GET | `/api/v1/books` | Get all books | 200 + list | 200 + 9 books | ✅ PASS |
| 3 | GET | `/api/v1/books/5` | Get by ID | 200 + book | 200 + "1984" | ✅ PASS |
| 4 | PUT | `/api/v1/books/10` | Update price & stock | 200 + updated | 200 + updated | ✅ PASS |
| 5 | GET | `/api/v1/books/category/Technology` | Filter: Technology | 200 + 3 books | 200 + 3 books | ✅ PASS |
| 5b | GET | `/api/v1/books/category/Fiction` | Filter: Fiction | 200 + 3 books | 200 + 3 books | ✅ PASS |
| 5c | GET | `/api/v1/books/category/Science` | Filter: Science | 200 + 2 books | 200 + 2 books | ✅ PASS |
| 5d | GET | `/api/v1/books/category/Self-Help` | Filter: Self-Help | 200 + 1 book | 200 + 1 book | ✅ PASS |
| 6 | GET | `/api/v1/books/search?author=Martin` | Search by author | 200 + match | 200 + 1 book | ✅ PASS |
| 6b | GET | `/api/v1/books/search?author=Orwell` | Search by author | 200 + match | 200 + 1 book | ✅ PASS |
| 6c | GET | `/api/v1/books/search?author=adams` | Case-insensitive | 200 + match | 200 + 1 book | ✅ PASS |
| 7 | DELETE | `/api/v1/books/10` | Delete book | 204 + gone | 204 + 404 confirmed | ✅ PASS |
| 8 | GET | `/api/v1/books/9999` | 404 Not Found | 404 | 404 | ✅ PASS |
| 9 | POST | `/api/v1/books` | 409 Duplicate ISBN | 409 | 409 | ✅ PASS |
| 10 | POST | `/api/v1/books` | 400 Validation error | 400 | 400 + 5 errors | ✅ PASS |
| 11 | PUT | `/api/v1/books/9999` | 404 Not Found on update | 404 | 404 | ✅ PASS |
| 12 | DELETE | `/api/v1/books/9999` | 404 Not Found on delete | 404 | 404 | ✅ PASS |

**Result: 17 / 17 tests passed ✅**

---

## Detailed Test Cases

---

### Test 1 — POST: Create a new book

**Request**
```http
POST /api/v1/books
Content-Type: application/json

{
  "title": "Atomic Habits",
  "author": "James Clear",
  "isbn": "978-0735211292",
  "price": 16.99,
  "stockQuantity": 200,
  "category": "Self-Help"
}
```

**Response — HTTP 201 Created ✅**
```json
{
  "id": 10,
  "title": "Atomic Habits",
  "author": "James Clear",
  "isbn": "978-0735211292",
  "price": 16.99,
  "stockQuantity": 200,
  "category": "Self-Help",
  "createdAt": "2026-11-02T19:15:29.149232",
  "updatedAt": "2026-11-02T19:15:29.149232"
}
```

---

### Test 2 — GET: All books

**Request**
```http
GET /api/v1/books
```

**Response — HTTP 200 OK ✅**
```json
[
  { "id": 1,  "title": "Clean Code",                           "author": "Robert C. Martin", "category": "Technology" },
  { "id": 2,  "title": "The Pragmatic Programmer",             "author": "David Thomas",     "category": "Technology" },
  { "id": 3,  "title": "Design Patterns",                      "author": "Gang of Four",     "category": "Technology" },
  { "id": 4,  "title": "To Kill a Mockingbird",                "author": "Harper Lee",       "category": "Fiction"    },
  { "id": 5,  "title": "1984",                                 "author": "George Orwell",    "category": "Fiction"    },
  { "id": 6,  "title": "A Brief History of Time",              "author": "Stephen Hawking",  "category": "Science"    },
  { "id": 7,  "title": "The Selfish Gene",                     "author": "Richard Dawkins",  "category": "Science"    },
  { "id": 8,  "title": "The Hitchhiker's Guide to the Galaxy", "author": "Douglas Adams",    "category": "Fiction"    },
  { "id": 10, "title": "Atomic Habits",                        "author": "James Clear",      "category": "Self-Help"  }
]
```

> 9 books returned (8 seeded + 1 created in Test 1).

---

### Test 3 — GET: Book by ID

**Request**
```http
GET /api/v1/books/5
```

**Response — HTTP 200 OK ✅**
```json
{
  "id": 5,
  "title": "1984",
  "author": "George Orwell",
  "isbn": "978-0451524935",
  "price": 10.99,
  "stockQuantity": 120,
  "category": "Fiction",
  "createdAt": "2026-11-02T18:23:28.036285",
  "updatedAt": "2026-11-02T18:23:28.036285"
}
```

---

### Test 4 — PUT: Update a book

**Request**
```http
PUT /api/v1/books/10
Content-Type: application/json

{
  "title": "Atomic Habits",
  "author": "James Clear",
  "isbn": "978-0735211292",
  "price": 19.99,
  "stockQuantity": 150,
  "category": "Self-Help"
}
```

**Response — HTTP 200 OK ✅**
```json
{
  "id": 10,
  "title": "Atomic Habits",
  "author": "James Clear",
  "isbn": "978-0735211292",
  "price": 19.99,
  "stockQuantity": 150,
  "category": "Self-Help",
  "createdAt": "2026-11-02T19:15:29.149232",
  "updatedAt": "2026-11-02T19:15:29.149232"
}
```

> Price: `$16.99 → $19.99` | Stock: `200 → 150`

---

### Test 5 — GET: Books by category

#### Technology
```http
GET /api/v1/books/category/Technology
```
**HTTP 200 OK ✅ — 3 books**
```
[1] Clean Code               — Robert C. Martin
[2] The Pragmatic Programmer — David Thomas
[3] Design Patterns          — Gang of Four
```

#### Fiction
```http
GET /api/v1/books/category/Fiction
```
**HTTP 200 OK ✅ — 3 books**
```
[4] To Kill a Mockingbird                — Harper Lee
[5] 1984                                 — George Orwell
[8] The Hitchhiker's Guide to the Galaxy — Douglas Adams
```

#### Science
```http
GET /api/v1/books/category/Science
```
**HTTP 200 OK ✅ — 2 books**
```
[6] A Brief History of Time — Stephen Hawking
[7] The Selfish Gene        — Richard Dawkins
```

#### Self-Help
```http
GET /api/v1/books/category/Self-Help
```
**HTTP 200 OK ✅ — 1 book**
```
[10] Atomic Habits — James Clear
```

---

### Test 6 — GET: Search by author (partial, case-insensitive)

| Query | Matched Book | Count |
|-------|-------------|-------|
| `?author=Martin` | Clean Code — Robert C. Martin | 1 |
| `?author=Orwell` | 1984 — George Orwell | 1 |
| `?author=Hawking` | A Brief History of Time — Stephen Hawking | 1 |
| `?author=Clear` | Atomic Habits — James Clear | 1 |
| `?author=adams` | The Hitchhiker's Guide to the Galaxy — Douglas Adams | 1 |

All searches returned **HTTP 200 OK ✅** with correct results.  
Case-insensitive matching confirmed (`adams` matched `Douglas Adams`).

---

### Test 7 — DELETE: Delete a book

**Request**
```http
DELETE /api/v1/books/10
```

**Response — HTTP 204 No Content ✅**
```
(empty body)
```

**Confirmation — follow-up GET:**
```http
GET /api/v1/books/10  →  HTTP 404 Not Found ✅
```

> Book permanently removed from PostgreSQL.

---

### Test 8 — GET: 404 Not Found

**Request**
```http
GET /api/v1/books/9999
```

**Response — HTTP 404 Not Found ✅**
```json
{
  "timestamp": "2026-11-02T19:02:06.1274906",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 9999",
  "path": "/api/v1/books/9999"
}
```

---

### Test 9 — POST: 409 Duplicate ISBN

**Request**
```http
POST /api/v1/books
Content-Type: application/json

{
  "title": "Another Clean Code",
  "author": "Someone",
  "isbn": "978-0132350884",
  "price": 25.00,
  "stockQuantity": 10,
  "category": "Technology"
}
```

**Response — HTTP 409 Conflict ✅**
```json
{
  "timestamp": "2026-11-02T19:02:13.4953352",
  "status": 409,
  "error": "Conflict",
  "message": "A book with ISBN '978-0132350884' already exists",
  "path": "/api/v1/books"
}
```

---

### Test 10 — POST: 400 Validation Error

**Request**
```http
POST /api/v1/books
Content-Type: application/json

{
  "title": "",
  "author": "",
  "isbn": "",
  "price": -5.00,
  "stockQuantity": -10
}
```

**Response — HTTP 400 Bad Request ✅**
```json
{
  "timestamp": "2026-11-02T19:02:23.3308126",
  "status": 400,
  "error": "Bad Request",
  "message": "Author is required; ISBN is required; Price must be greater than 0; Stock quantity cannot be negative; Title is required",
  "path": "/api/v1/books"
}
```

> All 5 bean-validation constraints fired and reported in a single response.

---

### Test 11 — PUT: 404 Not Found on Update

**Request**
```http
PUT /api/v1/books/9999
Content-Type: application/json

{ "title": "Ghost", "author": "Nobody", "isbn": "978-0000000000", "price": 1.00, "stockQuantity": 1, "category": "Unknown" }
```

**Response — HTTP 404 Not Found ✅**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 9999",
  "path": "/api/v1/books/9999"
}
```

---

### Test 12 — DELETE: 404 Not Found on Delete

**Request**
```http
DELETE /api/v1/books/9999
```

**Response — HTTP 404 Not Found ✅**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with id: 9999",
  "path": "/api/v1/books/9999"
}
```

---

## Final Verdict

| Category | Tests | Passed | Failed |
|----------|------:|-------:|-------:|
| Happy path — 2xx | 12 | 12 | 0 |
| Error handling — 4xx | 5 | 5 | 0 |
| **Total** | **17** | **17** | **0** |

**✅ All 17 tests passed. The Bookstore API is fully operational against PostgreSQL 18.**
