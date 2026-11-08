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

import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.ReviewService;

public class TestLibrary {

    private DatabaseService databaseService;
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
        Arguments.of("contains letter", "111222333444A"),
        Arguments.of("wrong check digit", "9780306406158"),
        Arguments.of("13 characters including hyphen", "978-030640615"));
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
        Arguments.of("double space", "AA  A"),
        Arguments.of("double dot", "AA..A"),
        Arguments.of("digit in middle", "A1A"),
        Arguments.of("starts with hyphen", "-AA"),
        Arguments.of("ends with hyphen", "AA-"),
        Arguments.of("starts with space", " AA"),
        Arguments.of("ends with space", "AA "),
        Arguments.of("starts with apostrophe", "'AA"),
        Arguments.of("ends with apostrophe", "AA'"),
        Arguments.of("starts with dot", ".AA"),
        Arguments.of("ends with dot", "AA."));
    }

    static Stream<Arguments> mixedSpecialCharacters() {
      return Stream.of(
        Arguments.of("dot and space", "A. B"),
        Arguments.of("hyphen, space and dot", "A- .B"),
        Arguments.of("space and apostrophe", "A 'B"),
        Arguments.of("dot, space, dot", "A. .B"),
        Arguments.of("hyphen, apostrophe, hyphen", "A-'-B"),
        Arguments.of("space, apostrophe, space", "A ' B"));
    }

    static Stream<Arguments> invalidTitle() {
      return Stream.of(
        Arguments.of("null title", null),
        Arguments.of("empty title", ""));
    }

    @BeforeEach
    void init(){
        databaseService = mock(DatabaseService.class);
        reviewService = mock(ReviewService.class);
        library = new Library(databaseService, reviewService);

    }

    //AddBook - Happy Path

    @Test
    void GivenValidBook_WhenAddBook_ThenBookAddedToDatabase() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenAuthorWithSingleHyphen_WhenAddBook_ThenBookAddedToDatabase() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "Mary-Jane");
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenAuthorWithSingleApostrophe_WhenAddBook_ThenBookAddedToDatabase() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "O'Connor");
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenAuthorWithPeriod_WhenAddBook_ThenBookAddedToDatabase() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, "J.Rowling");
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenISBNWithCheckDigit0_WhenAddBook_ThenBookAddedToDatabase(){
        String isbn = "9780200000000";
        Book book = new Book(isbn , VALID_TITLE, VALID_AUTHOR);
        library.addBook(book);
        verify(databaseService).addBook(isbn, book);
    }

    @Test
    void GivenISBNWithHyphens_WhenAddBook_ThenBookAddedToDatabase(){
        String isbn = "978-030-640-6157";
        Book book = new Book(isbn , VALID_TITLE, VALID_AUTHOR);
        library.addBook(book);
        verify(databaseService).addBook(isbn, book);
    }

    @Test
    void GivenSingleCharacterAuthor_WhenAddBook_ThenBookAddedToDatabase(){
        Book book = new Book(VALID_ISBN , VALID_TITLE, "A");
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenNonASCIILetterAuthor_WhenAddBook_ThenBookAddedToDatabase(){
        Book book = new Book(VALID_ISBN , VALID_TITLE, "José");
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("mixedSpecialCharacters")
    void GivenMixedNonConsecutiveSpecialCharactersAuthor_WhenAddBook_ThenBookAddedToDatabase(String label, String author){
        Book book = new Book(VALID_ISBN , VALID_TITLE, author);
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @Test
    void GivenWhitespaceOnlyTitle_WhenAddBook_ThenBookAddedToDatabase(){
        Book book = new Book(VALID_ISBN , "   ", VALID_AUTHOR);
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    //AddBook - Sad Path

    @Test
    void GivenNullBook_WhenAddBook_ThenThrowsIllegalArgumentException(){
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()->library.addBook(null));
        assertEquals("Invalid book.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    //Invalid ISBN
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidISBN")
    void GivenInvalidISBN_WhenAddBook_ThenThrowsInvalidISBN(String label, String ISBN) {
        Book book = new Book(ISBN, VALID_TITLE, VALID_AUTHOR);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid ISBN.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    //Invalid Title
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTitle")
    void GivenInvalidTitle_WhenAddBook_ThenThrowsIllegalArgumentException(String label, String title){
        Book book = new Book(VALID_ISBN, title, VALID_AUTHOR);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
        assertEquals("Invalid title.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    //Invalid Author
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidAuthors")
    void GivenInvalidAuthor_WhenAddBook_ThenThrowsInvalidAuthor(String label, String author) {
        Book book = new Book(VALID_ISBN, "The Hobbit", author);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Invalid author.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    @Test
    void GivenBookAlreadyBorrowed_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        book.borrow();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
        assertEquals("Book with invalid borrowed state.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    @Test
    void GivenAlreadyExistsBook_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertEquals("Book already exists.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    
}