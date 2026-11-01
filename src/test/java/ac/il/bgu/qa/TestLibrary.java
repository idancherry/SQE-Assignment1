package ac.il.bgu.qa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;

import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.ReviewService;

public class TestLibrary {

    private DatabaseService dbService;
    private ReviewService reviewService;
    private Library library;

    private static final String VALID_ISBN = "9780306406157";
    private static final String VALID_TITLE = "Harry Potter";
    private static final String VALID_AUTHOR = "JK Rowling";

    static Stream<Arguments> invalidISBN() {
      return Stream.of(
        Arguments.of("null", null),
        Arguments.of("empty", ""),
        Arguments.of("short", "123"),
        Arguments.of("long", "11122233344455"),
        Arguments.of("contains letter", "111222333444A"));
    }

    static Stream<Arguments> invalidAuthors() {
      return Stream.of(
        Arguments.of("null", null),
        Arguments.of("empty", ""),
        Arguments.of("starts with digit", "0AAA"),
        Arguments.of("ends with digit", "AAA0"),
        Arguments.of("contains symbol", "AA#A"),
        Arguments.of("double hyphen", "AA--A"),
        Arguments.of("double apostrophe", "AA''A"),
        Arguments.of("digit in middle", "A1A"));
    }

    @BeforeEach
    void init(){
        dbService = mock(DatabaseService.class);
        reviewService = mock(ReviewService.class);
        library = new Library(dbService, reviewService);

    }

    //AddBook - Happy Path

    @Test
    void GivenValidBook_WhenAddBook_ThenBookAddedToDatabase() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        library.addBook(book);
        verify(dbService).addBook(VALID_ISBN, book);
    }

    //AddBook - Sad Path

    @Test
    void GivenNullBook_WhenAddBook_ThenThrowsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, ()->library.addBook(null));
    }

    //Invalid ISBN
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidISBN")
    void GivenInvalidISBN_WhenAddBook_ThenThrowsInvalidISBN(String label, String ISBN) {
        Book book = new Book(ISBN, VALID_TITLE, VALID_AUTHOR);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid ISBN.", e.getMessage());
        verify(dbService, never()).addBook(any(), any());
    }

    //Invalid Title
    @Test
    void GivenNullTitle_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, null, VALID_AUTHOR);
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenEmptyTitle_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, "", VALID_AUTHOR);
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    //Invalid Author
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidAuthors")
    void GivenInvalidAuthor_WhenAddBook_ThenThrowsInvalidAuthor(String label, String author) {
        Book book = new Book(VALID_ISBN, "The Hobbit", author);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid author.", e.getMessage());
        verify(dbService, never()).addBook(any(), any());
    }

    @Test
    void GivenBookAlreadyBorrowed_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        book.borrow();
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenAlreadyExistsBook_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(dbService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Book already exists.", e.getMessage());
        verify(dbService, never()).addBook(any(), any());
    }

    
}