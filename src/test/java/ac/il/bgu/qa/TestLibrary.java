package ac.il.bgu.qa;

import ac.il.bgu.qa.errors.*;
import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.NotificationService;
import ac.il.bgu.qa.services.ReviewService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.mockito.*;

import java.util.Collections;
import java.util.List;

public class TestLibrary {
    DatabaseService databaseService;
    ReviewService reviewService;
    Library library;
    NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        databaseService = Mockito.mock(DatabaseService.class);
        reviewService = Mockito.mock(ReviewService.class);
        library = new Library(databaseService, reviewService);
        notificationService = Mockito.mock(NotificationService.class);
    }


    //addBook


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

//RegisterUser

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

//notifyUserWithBookReviews

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


//GetBookByISBN

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

//addBook?

//    @Test
//    void GivenNullName_WhenAddBook_ThenThrowIllegalArgumentException() {
//        Book book = new Book("9780306406157", "Harry potter", null);
//
//        Assertions.assertThrows(
//                IllegalArgumentException.class,
//                () -> library.addBook(book));
//    }
//
//    @Test
//    void GivenEmptyName_WhenAddBook_ThenThrowIllegalArgumentException() {
//        Book book = new Book("9780306406157", "Harry potter", "");
//
//        Assertions.assertThrows(
//                IllegalArgumentException.class,
//                () -> library.addBook(book));
//    }
//
//    @Test
//    void GivenAuthorStartingWithDigit_WhenAddBook_ThenThrowIllegalArgumentException() {
//        Book book = new Book("9780306406157", "Harry potter", "1arry");
//
//        Assertions.assertThrows(
//                IllegalArgumentException.class,
//                () -> library.addBook(book));
//    }
//
//    @Test
//    void GivenAuthorEndingWithDigit_WhenAddBook_ThenThrowIllegalArgumentException() {
//        Book book = new Book("9780306406157", "Harry potter", "Harr1");
//
//        Assertions.assertThrows(
//                IllegalArgumentException.class,
//                () -> library.addBook(book));
//    }
//
//    @Test
//    void GivenAuthorWithInvalidCharacter_WhenAddBook_ThenThrowIllegalArgumentException() {
//        Book book = new Book("9780306406157", "Harry potter", "Ha*ry");
//
//        Assertions.assertThrows(
//                IllegalArgumentException.class,
//                () -> library.addBook(book));
//    }
//
//    @Test
//    void GivenAuthorWithConsecutiveHyphens_WhenAddBook_ThenThrowIllegalArgumentException() {
//        Book book = new Book("9780306406157", "Harry potter", "Ha--ry");
//
//        Assertions.assertThrows(
//                IllegalArgumentException.class,
//                () -> library.addBook(book));
//    }
//
//    @Test
//    void GivenAuthorWithConsecutiveApostrophes_WhenAddBook_ThenThrowIllegalArgumentException() {
//        Book book = new Book("9780306406157", "Harry potter", "Ha''ry");
//
//        Assertions.assertThrows(
//                IllegalArgumentException.class,
//                () -> library.addBook(book));
//    }


}
