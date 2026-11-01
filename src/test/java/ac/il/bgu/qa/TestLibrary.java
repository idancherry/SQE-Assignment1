package ac.il.bgu.qa;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.mockito.*;

import ac.il.bgu.qa.services.DatabaseService;
import ac.il.bgu.qa.services.ReviewService;

public class TestLibrary {

    private DatabaseService dbService;
    private ReviewService reviewService;
    private Library library;

    @BeforeEach
    void init(){
        dbService = mock(DatabaseService.class);
        reviewService = mock(ReviewService.class);
        library = new Library(dbService, reviewService);

    }

    @Test
    void GivenNullBook_WhenaddBook_ThenThrowsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, ()->library.addBook(null));
    }

    @Test
    void GivenNullISBN_WhenaddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book(null, "Harry Potter", "JK Rowling");
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenShortISBN_WhenaddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book("aaa", "Harry Potter", "JK Rowling");
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenLongISBN_WhenaddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book("aaabbbcccdddee", "Harry Potter", "JK Rowling");
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenNullTitle_WhenaddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book("aaabbbcccddde", null, "JK Rowling");
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenEmptyTitle_WhenaddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book("aaabbbcccddd1", "", "JK Rowling");
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenNullAuthor_WhenaddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book("aaabbbcccddd2", "The Hobbit", null);
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

    @Test
    void GivenEmptyAuthor_WhenaddBook_ThenThrowsIllegalArgumentException(){
        Book book = new Book("aaabbbcccddd3", "The Hobbit", "");
        assertThrows(IllegalArgumentException.class, ()->library.addBook(book));
    }

}