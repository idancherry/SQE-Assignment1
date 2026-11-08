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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ac.il.bgu.qa.errors.BookAlreadyBorrowedException;
import ac.il.bgu.qa.errors.BookNotBorrowedException;
import ac.il.bgu.qa.errors.BookNotFoundException;
import ac.il.bgu.qa.errors.NotificationException;
import ac.il.bgu.qa.errors.NoReviewsFoundException;
import ac.il.bgu.qa.errors.ReviewException;
import ac.il.bgu.qa.errors.ReviewServiceUnavailableException;
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

    private static final String VALID_ISBN = "9780306406157";
    private static final String VALID_TITLE = "Harry Potter";
    private static final String VALID_AUTHOR = "JK Rowling";
    private static final String VALID_USERID = "123456789012";
    private static final String VALID_USERNAME = "Idan";

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

    static Stream<Arguments> invalidUsername() {
      return Stream.of(
        Arguments.of("null", null),
        Arguments.of("empty", ""));
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

    @BeforeEach
    public void setUp() {
        databaseService = mock(DatabaseService.class);
        reviewService = mock(ReviewService.class);
        library = new Library(databaseService, reviewService);
        notificationService = mock(NotificationService.class);
    }

    @Test
    void GivenNullUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUserId")
    void GivenInvalidId_WhenRegisterUser_ThenThrowIllegalArgumentException(String label, String id) {
        User user = new User(VALID_USERNAME, id, notificationService);

        assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUsername")
    void GivenInvalidUsername_WhenRegisterUser_ThenThrowIllegalArgumentException(String label, String name) {
        User user = new User(name, VALID_USERID, notificationService);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenNullNotification_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(VALID_USERNAME, VALID_USERID, null);
        assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
    }

    @Test
    void GivenExistingUser_WhenRegisterUser_ThenThrowIllegalArgumentException() {
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        when(databaseService.getUserById(VALID_USERID))
                .thenReturn(user);
        assertThrows(
                IllegalArgumentException.class,
                () -> library.registerUser(user));
        verify(databaseService, never())
                .registerUser(VALID_USERID, user);
    }

    @Test
    void GivenValidNewUser_WhenRegisterUser_ThenRegisterUserInDatabase() {
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        when(databaseService.getUserById(VALID_USERID))
                .thenReturn(null);
        library.registerUser(user);
        verify(databaseService).registerUser(VALID_USERID, user);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidISBN")
    void GivenInvalidISBN_WhennotifyUserWithBookReviews_ThenThrowIllegalArgumentException(String label, String isbn) {
        assertThrows(
                IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(isbn, VALID_USERID));

        verifyNoInteractions(reviewService, notificationService);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUserId")
    void GivenNullUserID_WhennotifyUserWithBookReviews_ThenThrowIllegalArgumentException(String label, String id) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, id));

        verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenBookNotFound_WhenNotifyUserWithBookReviews_ThenThrowBookNotFoundException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(null);
        assertThrows(
                BookNotFoundException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID));

        verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenUserNotFound_WhenNotifyUserWithBookReviews_ThenThrowUserNotRegisteredException() {
        Book book = new Book(VALID_ISBN,  VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(null);
        assertThrows(
                UserNotRegisteredException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID));

        verifyNoInteractions(reviewService, notificationService);
    }

    @Test
    void GivenNullReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(null);

        assertThrows(
                NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID));

        verifyNoInteractions(notificationService);
        verify(reviewService).close();
    }

    @Test
    void GivenEmptyReviews_WhenNotifyUserWithBookReviews_ThenThrowNoReviewsFoundException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);

        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(Collections.emptyList());

        assertThrows(
                NoReviewsFoundException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID));

        verifyNoInteractions(notificationService);
        verify(reviewService).close();
    }

    @Test
    void GivenReviewException_WhenNotifyUserWithBookReviews_ThenThrowReviewServiceUnavailableException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);

        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenThrow(new ReviewException("Review failed"));

        assertThrows(
                ReviewServiceUnavailableException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID));

        verifyNoInteractions(notificationService);
        verify(reviewService).close();
    }

    @Test
    void GivenValidReviews_WhenNotifyUserWithBookReviews_ThenSendNotificationOnce() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(reviews);

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";
        library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID);

        verify(notificationService).notifyUser(VALID_USERID, expectedMessage);
        verify(reviewService).close();
    }

    @Test
    void GivenFirstNotificationFails_WhenNotifyUserWithBookReviews_ThenSucceedOnSecondAttempt() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";

        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(reviews);

        doThrow(new NotificationException("Send failed"))
                .doNothing()
                .when(notificationService).notifyUser(VALID_USERID, expectedMessage);

        library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID);

        verify(notificationService, times(2))
                .notifyUser(VALID_USERID, expectedMessage);
        verify(reviewService).close();
    }

    @Test
    void GivenAllNotificationAttemptsFail_WhenNotifyUserWithBookReviews_ThenThrowNotificationException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";

        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(reviews);

        doThrow(new NotificationException("Send failed"))
                .when(notificationService).notifyUser(VALID_USERID, expectedMessage);

        assertThrows(
                NotificationException.class,
                () -> library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID));

        verify(notificationService, times(5))
                .notifyUser(VALID_USERID, expectedMessage);
        verify(reviewService).close();
    }

    @Test
    void GivenFirstFourNotificationsFail_WhenNotifyUserWithBookReviews_ThenSucceedOnFifthAttempt() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";

        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(reviews);

        doThrow(new NotificationException("Send failed"))
                .doThrow(new NotificationException("Send failed"))
                .doThrow(new NotificationException("Send failed"))
                .doThrow(new NotificationException("Send failed"))
                .doNothing()
                .when(notificationService).notifyUser(VALID_USERID, expectedMessage);

        library.notifyUserWithBookReviews(VALID_ISBN, VALID_USERID);

        verify(notificationService, times(5))
                .notifyUser(VALID_USERID, expectedMessage);
        verify(reviewService).close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidISBN")
    void GivenInvalidISBN_WhenGetBookByISBN_ThenReturnBookAndSendReviews(String label, String isbn) {
        assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(isbn, VALID_USERID));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUserId")
    void GivenNullUserID_WhenGetBookByISBN_ThenThrowIllegalArgumentException(String label, String id) {
        assertThrows(IllegalArgumentException.class,
                () -> library.getBookByISBN(VALID_ISBN, id));
    }

    @Test
    void GivenNullBook_WhenGetBookByISBN_ThenThrowBookNotFoundException() {
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(null);
        Assertions.assertThrows(BookNotFoundException.class,
                () -> library.getBookByISBN(VALID_ISBN, VALID_USERID));
    }

    @Test
    void GivenBorrowedBook_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        book.borrow();
        Assertions.assertThrows(BookAlreadyBorrowedException.class,
                () -> library.getBookByISBN(VALID_ISBN, VALID_USERID));
    }

    @Test
    void GivenAvailableBook_WhenGetBookByISBN_ThenThrowBookAlreadyBorrowedException() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        User user = new User(VALID_USERNAME, VALID_USERID, notificationService);
        List<String> reviews = List.of("Great book", "Worth reading");

        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(user);
        when(reviewService.getReviewsForBook(VALID_ISBN)).thenReturn(reviews);
        String expectedMessage = "Reviews for 'Harry Potter':\nGreat book\nWorth reading";
        Book result = library.getBookByISBN(VALID_ISBN, VALID_USERID);
        Assertions.assertSame(book, result);
        verify(notificationService).notifyUser(VALID_USERID, expectedMessage);
        verify(reviewService).close();
    }

    @Test
    void GivenUserNotRegistered_WhenGetBookByISBN_ThenReturnBook() {
        Book book = new Book(VALID_ISBN, VALID_TITLE, VALID_AUTHOR);
        when(databaseService.getBookByISBN(VALID_ISBN)).thenReturn(book);
        when(databaseService.getUserById(VALID_USERID)).thenReturn(null);
        Book result = library.getBookByISBN(VALID_ISBN, VALID_USERID);
        Assertions.assertSame(book, result);
        verifyNoInteractions(reviewService, notificationService);
    }

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