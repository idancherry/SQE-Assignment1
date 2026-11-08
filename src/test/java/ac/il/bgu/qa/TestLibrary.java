package ac.il.bgu.qa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import ac.il.bgu.qa.errors.BookAlreadyBorrowedException;
import ac.il.bgu.qa.errors.BookNotBorrowedException;
import ac.il.bgu.qa.errors.BookNotFoundException;
import ac.il.bgu.qa.errors.UserNotRegisteredException;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;

import java.util.Collections;
import java.util.List;

public class TestLibrary {
    private DatabaseService databaseService;
    private ReviewService reviewService;
    private Library library;
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        databaseService = Mockito.mock(DatabaseService.class);
        reviewService = Mockito.mock(ReviewService.class);
        library = new Library(databaseService, reviewService);
        notificationService = Mockito.mock(NotificationService.class);
    }



    @Test
    void GivenAuthorWithSingleHyphen_WhenAddBook_ThenBookAddedToDatabase() {
        String isbn = "9780306406157";
        Book book = new Book(isbn, "Harry Potter", "Mary-Jane");

        library.addBook(book);

        Mockito.verify(databaseService).addBook(isbn, book);
    }

    @Test
    void GivenAuthorWithSingleApostrophe_WhenAddBook_ThenBookAddedToDatabase() {
        String isbn = "9780306406157";
        Book book = new Book(isbn, "Harry Potter", "O'Connor");

        library.addBook(book);

        Mockito.verify(databaseService).addBook(isbn, book);
    }

    @Test
    void GivenAuthorWithPeriod_WhenAddBook_ThenBookAddedToDatabase() {
        String isbn = "9780306406157";
        Book book = new Book(isbn, "Harry Potter", "J.Rowling");

        library.addBook(book);

        Mockito.verify(databaseService).addBook(isbn, book);
    }

    @Test
    void GivenInvalidISBNCheckDigit_WhenAddBook_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406158";
        Book book = new Book(isbn, "Harry Potter", "Rowling");

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.addBook(book));

        Mockito.verify(databaseService, Mockito.never())
                .addBook(Mockito.any(), Mockito.any());
    }


    @Test
    void GivenNullUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(null));
    }

    @Test
    void GivenNullId_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("Dani", null, notificationService);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenEmptyId_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("Dani", "", notificationService);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenTooShortID_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("Dani", "12345678901", notificationService);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenTooLongID_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("Dani", "1234567890111", notificationService);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenNonDigitID_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("Dani", "1234*7890111", notificationService);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenNullName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(null, "123456789011", notificationService);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenEmptyName_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("", "123456789011", notificationService);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenNullNotification_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("Dani", "123456789011", null);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenExistingUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User("Dani", "123456789011", notificationService);

        Mockito.when(databaseService.getUserById("123456789011"))
                .thenReturn(user);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));

        Mockito.verify(databaseService, Mockito.never())
                .registerUser("123456789011", user);
    }

    @Test
    void GivenValidNewUser_WhenRegisterUser_ThenRegisterUserInDatabase() {
        User user = new User("Dani", "123456789011", notificationService);

        Mockito.when(databaseService.getUserById("123456789011"))
                .thenReturn(null);

        library.registerUser(user);

        Mockito.verify(databaseService).registerUser("123456789011", user);
    }


    @Test
    void GivenInvalidISBN_WhennotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        String isbn = "111";
        String userId = "123456789012";

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenNullUserID_WhennotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = null;

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenTooShortUserID_WhennotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = "12345678901";

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenTooLongUserID_WhennotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = "1234567890123";

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenUserIDWithNonDigit_WhennotifyUserWithBookReviews_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = "123*56789012";

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenBookNotFound_WhenNotifyUserWithBookReviews_ThenThrowBookNotFoundException() {
        String isbn = "9780306406157";
        String userId = "123456789012";

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(null);

        Assertions.assertThrows(
                BookNotFoundException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenUserNotFound_WhenNotifyUserWithBookReviews_ThenThrowUserNotRegisteredException() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(null);


        Assertions.assertThrows(
                UserNotRegisteredException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenNullReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenReturn(null);

        Assertions.assertThrows(
                NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(notificationService);
        Mockito.verify(reviewService).close();
    }

    @Test
    void GivenEmptyReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenReturn(Collections.emptyList());

        Assertions.assertThrows(
                NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(notificationService);
        Mockito.verify(reviewService).close();
    }

    @Test
    void GivenReviewException_WhenNotifyUserWithBookReviews_ThenThrowReviewServiceUnavailableException() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenThrow(new ReviewException("Review failed"));

        Assertions.assertThrows(
                ReviewServiceUnavailableException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verifyNoInteractions(notificationService);
        Mockito.verify(reviewService).close();
    }

    @Test
    void GivenValidReviews_WhenNotifyUserWithBookReviews_ThenSendNotificationOnce() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenReturn(reviews);

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";
        library.notifyUserWithBookReviews(isbn, userId);

        Mockito.verify(notificationService).notifyUser(userId, expectedMessage);
        Mockito.verify(reviewService).close();
    }

    @Test
    void GivenFirstNotificationFails_WhenNotifyUserWithBookReviews_ThenSucceedOnSecondAttempt() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenReturn(reviews);

        Mockito.doThrow(new NotificationException("Send failed"))
                .doNothing()
                .when(notificationService).notifyUser(userId, expectedMessage);

        library.notifyUserWithBookReviews(isbn, userId);

        Mockito.verify(notificationService, Mockito.times(2))
                .notifyUser(userId, expectedMessage);
        Mockito.verify(reviewService).close();
    }

    @Test
    void GivenAllNotificationAttemptsFail_WhenNotifyUserWithBookReviews_ThenThrowNotificationException() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenReturn(reviews);

        Mockito.doThrow(new NotificationException("Send failed"))
                .when(notificationService).notifyUser(userId, expectedMessage);

        Assertions.assertThrows(
                NotificationException.class,
                () -> library.notifyUserWithBookReviews(isbn, userId));

        Mockito.verify(notificationService, Mockito.times(5))
                .notifyUser(userId, expectedMessage);
        Mockito.verify(reviewService).close();
    }

    @Test
    void GivenFirstFourNotificationsFail_WhenNotifyUserWithBookReviews_ThenSucceedOnFifthAttempt() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenReturn(reviews);

        Mockito.doThrow(new NotificationException("Send failed"))
                .doThrow(new NotificationException("Send failed"))
                .doThrow(new NotificationException("Send failed"))
                .doThrow(new NotificationException("Send failed"))
                .doNothing()
                .when(notificationService).notifyUser(userId, expectedMessage);

        library.notifyUserWithBookReviews(isbn, userId);

        Mockito.verify(notificationService, Mockito.times(5))
                .notifyUser(userId, expectedMessage);
        Mockito.verify(reviewService).close();
    }



    @Test
    void GivenInvalidISBN_WhenGetBookByISBN_ThenReturnBookAndSendReviews() {
        String isbn = "780306406157";
        String userId = "123456789012";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, userId));

    }

    @Test
    void GivenNullUserID_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, null));
    }

    @Test
    void GivenEmptyUserID_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = "";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, userId));
    }

    @Test
    void GivenTooShortUserID_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = "12345678901";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, userId));
    }

    @Test
    void GivenTooLongUserID_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = "1234567890123";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, userId));
    }

    @Test
    void GivenNonDigitUserID_WhenGetBookByISBN_ThenThrowIllegalArgumentException() {
        String isbn = "9780306406157";
        String userId = "1234*6789012";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, userId));
    }

    @Test
    void GivenNullBook_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        String isbn = "9780306406157";
        String userId = "123456789012";

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(null);

        Assertions.assertThrows(BookNotFoundException.class,
                () -> library.getBookByISBN(isbn, userId));

    }

    @Test
    void GivenBorrowedBook_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        book.borrow();

        Assertions.assertThrows(BookAlreadyBorrowedException.class,
                () -> library.getBookByISBN(isbn, userId));
    }

    @Test
    void GivenAvailableBook_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");
        User user = new User("Dani", "123456789012", notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(user);
        Mockito.when(reviewService.getReviewsForBook(isbn)).thenReturn(reviews);

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";

        Book result = library.getBookByISBN(isbn, userId);

        Assertions.assertSame(book, result);

        Mockito.verify(notificationService).notifyUser(userId, expectedMessage);
        Mockito.verify(reviewService).close();
    }

    @Test
    void GivenUserNotRegistered_WhenGetBookByISBN_ThenReturnBook() {
        String isbn = "9780306406157";
        String userId = "123456789012";
        Book book = new Book(isbn,  "Harry Potter", "J.K. Rowling");

        Mockito.when(databaseService.getBookByISBN(isbn)).thenReturn(book);
        Mockito.when(databaseService.getUserById(userId)).thenReturn(null);

        Book result = library.getBookByISBN(isbn, userId);

        Assertions.assertSame(book, result);
        Mockito.verifyNoInteractions(reviewService, notificationService);
    }

    private static final String VALID_ISBN = "9780306406157";
    private static final String VALID_TITLE = "Harry Potter";
    private static final String VALID_AUTHOR = "JK Rowling";
    private static final String VALID_USERID = "123456789012";
    private static final String VALID_USERNAME = "Idan";


    static Stream<Arguments> validAuthors() {
      return Stream.of(
        Arguments.of("hyphen", "Mary-Jane"),
        Arguments.of("apostrophe", "O'Connor"),
        Arguments.of("dot", "J.Rowling"),
        Arguments.of("space", VALID_AUTHOR),
        Arguments.of("single character", "A"),
        Arguments.of("non ASCII letters", "José"));
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

    static Stream<Arguments> invalidTitle() {
      return Stream.of(
        Arguments.of("null title", null),
        Arguments.of("empty title", ""));
    }

    static Stream<Arguments> invalidUserId() {
      return Stream.of(
        Arguments.of("null", null),
        Arguments.of("short", "012"),
        Arguments.of("long", "1231231231231" ),
        Arguments.of("contains letters", "12a123123123"),
        Arguments.of("contains special symbols", "1212@3123123"),
        Arguments.of("contains space with 11 digits", "12312 123123"),
        Arguments.of("contains space with 12 digits", "123123 123123"),
        Arguments.of("empty", ""),
        Arguments.of("11 digits", "12345678901"));
    }

    //AddBook - Happy Path

    @Test
    void GivenValidBook_WhenAddBook_ThenBookAddedToDatabase() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validAuthors")
    void GivenValidAuthor_WhenAddBook_ThenBookAddedToDatabase(String label, String author) {
        Book book = new Book(VALID_ISBN, VALID_TITLE, author);
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
    void GivenWhitespaceOnlyTitle_WhenAddBook_ThenBookAddedToDatabase(){
        Book book = new Book(VALID_ISBN , "   ", VALID_AUTHOR);
        library.addBook(book);
        verify(databaseService).addBook(VALID_ISBN, book);
    }

    //AddBook - Sad Path

    @Test
    void GivenNullBook_WhenAddBook_ThenThrowsIllegalArgumentException(){
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            ()->library.addBook(null));
        assertEquals("Invalid book.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    //Invalid ISBN
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidISBN")
    void GivenInvalidISBN_WhenAddBook_ThenThrowsInvalidISBN(String label, String ISBN) {
        Book book = new Book(ISBN, VALID_TITLE, VALID_AUTHOR);
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(book));
        assertEquals("Invalid ISBN.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    //Invalid Title
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTitle")
    void GivenInvalidTitle_WhenAddBook_ThenThrowsIllegalArgumentException(String label, String title){
        Book book = new Book(VALID_ISBN, title, VALID_AUTHOR);
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            ()->library.addBook(book));
        assertEquals("Invalid title.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    //Invalid Author
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidAuthors")
    void GivenInvalidAuthor_WhenAddBook_ThenThrowsInvalidAuthor(String label, String author) {
        Book book = new Book(VALID_ISBN, VALID_TITLE, author);
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(book));
        assertEquals("Invalid author.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    @Test
    void GivenBookAlreadyBorrowed_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        book.borrow();
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            ()->library.addBook(book));
        assertEquals("Book with invalid borrowed state.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    @Test
    void GivenAlreadyExistsBook_WhenAddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(book));
        assertEquals("Book already exists.", e.getMessage());
        verify(databaseService, never()).addBook(any(), any());
    }


    //borrowBook - Happy Path

    @Test
    void GivenValidBookAndUser_WhenBorrowBook_ThenBookMarkedBorrowedAndDatabaseUpdated(){
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        library.borrowBook(VALID_ISBN, VALID_USERID);
        assertTrue(book.isBorrowed());
        verify(databaseService).borrowBook(VALID_ISBN, VALID_USERID);
    }

    //borrowBook - Sad Path 
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidISBN")
    void GivenInvalidISBN_WhenBorrowBook_ThenThrowsIllegalArgumentException(String label, String isbn){
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> library.borrowBook(isbn, VALID_USERID));
        assertEquals("Invalid ISBN.", e.getMessage());
        verify(databaseService, never()).borrowBook(any(), any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    @Test 
    void GivenBookDoesntExist_WhenBorrowBook_ThenThrowsBookNotFoundException(){
        BookNotFoundException e = assertThrows(
            BookNotFoundException.class,
            () -> library.borrowBook(VALID_ISBN, VALID_USERID));
        assertEquals("Book not found!", e.getMessage());
        verify(databaseService, never()).getUserById(any());
        verify(databaseService, never()).borrowBook(any(), any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUserId")
    void GivenInvalidUserId_WhenBorrowBook_ThenThrowsIllegalArgumentException(String label, String userId){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> library.borrowBook(VALID_ISBN, userId));
        assertEquals("Invalid user Id.", e.getMessage());
        assertFalse(book.isBorrowed());
        verify(databaseService, never()).getUserById(any());
        verify(databaseService, never()).borrowBook(any(), any());
    }

    @Test
    void GivenUserDoesntExist_WhenBorrowBook_ThenThrowsUserNotRegisteredException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        UserNotRegisteredException e = assertThrows(
            UserNotRegisteredException.class,
            () -> library.borrowBook(VALID_ISBN, VALID_USERID));
        assertEquals("User not found!", e.getMessage());
        assertFalse(book.isBorrowed());
        verify(databaseService, never()).borrowBook(any(), any());
    }

    @Test
    void GivenBookAlreadyBorrowed_WhenBorrowBook_ThenThrowsBookAlreadyBorrowedException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        book.borrow();
        BookAlreadyBorrowedException e = assertThrows(
            BookAlreadyBorrowedException.class,
            () -> library.borrowBook(VALID_ISBN, VALID_USERID));
        assertEquals("Book is already borrowed!", e.getMessage());
        verify(databaseService, never()).borrowBook(any(), any());
    }

    @Test
    void GivenBookDoesntExistAndInvalidUserId_WhenBorrowBook_ThenThrowsBookNotFoundException() {
        assertThrows(BookNotFoundException.class,
            () -> library.borrowBook(VALID_ISBN, "bad"));
        verify(databaseService, never()).getUserById(any());
    }


    //returnBook - Happy Path

    @Test
    void GivenBorrowedBook_WhenReturnBook_ThenBookMarkedReturnedAndDatabaseUpdated(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        book.borrow();
        library.returnBook(VALID_ISBN);
        assertFalse(book.isBorrowed());
        verify(databaseService).returnBook(VALID_ISBN);
    }


    //returnBook - Sad Path

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidISBN")
    void GivenInvalidISBN_WhenReturnBook_ThenThrowsIllegalArgumentException(String label, String isbn){
        IllegalArgumentException e = assertThrows(
            IllegalArgumentException.class,
            () -> library.returnBook(isbn));
        assertEquals("Invalid ISBN.", e.getMessage());
        verify(databaseService, never()).returnBook(any());
        verify(databaseService, never()).getBookByISBN(any());
    }

    @Test 
    void GivenBookDoesntExist_WhenReturnBook_ThenThrowsBookNotFoundException(){
        BookNotFoundException e = assertThrows(
            BookNotFoundException.class,
            () -> library.returnBook(VALID_ISBN));
        assertEquals("Book not found!", e.getMessage());
        verify(databaseService, never()).returnBook(any());
    }

    @Test
    void GivenBookNotBorrowed_WhenReturnBook_ThenThrowsBookNotBorrowedException(){
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        BookNotBorrowedException e = assertThrows(
            BookNotBorrowedException.class,
            () -> library.returnBook(VALID_ISBN));
        assertEquals("Book wasn't borrowed!", e.getMessage());
        verify(databaseService, never()).returnBook(any());
    }

}
