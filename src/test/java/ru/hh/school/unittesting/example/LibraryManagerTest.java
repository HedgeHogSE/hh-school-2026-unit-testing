package ru.hh.school.unittesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.homework.LibraryManager;
import ru.hh.school.unittesting.homework.NotificationService;
import ru.hh.school.unittesting.homework.UserService;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LibraryManager libraryManager;

    @Test
    void addBookShouldAddNewBookToInventory() {
        libraryManager.addBook("book1", 5);

        assertEquals(5, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    void addBookShouldNotAffectOtherBooks() {
        libraryManager.addBook("book1", 3);
        libraryManager.addBook("book2", 7);

        assertEquals(3, libraryManager.getAvailableCopies("book1"));
        assertEquals(7, libraryManager.getAvailableCopies("book2"));
    }

    @Test
    void borrowBookShouldReturnTrueWhenUserIsActiveAndBookAvailable() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.addBook("book1", 1);

        boolean result = libraryManager.borrowBook("book1", "user1");

        assertTrue(result);
    }

    @Test
    void borrowBookShouldReturnFalseWhenBookNotAvailable() {
        when(userService.isUserActive("user1")).thenReturn(true);

        boolean result = libraryManager.borrowBook("book1", "user1");

        assertFalse(result);
        verify(notificationService, never()).notifyUser(anyString(), anyString());
    }

    @Test
    void borrowBookShouldDecrementAvailableCopies() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.addBook("book1", 3);

        libraryManager.borrowBook("book1", "user1");

        assertEquals(2, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    void borrowBookShouldNotifyUserOnSuccess() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.addBook("book1", 1);

        libraryManager.borrowBook("book1", "user1");

        verify(notificationService).notifyUser("user1", "You have borrowed the book: book1");
    }

    @Test
    void borrowBookShouldNotDecrementCopiesWhenUserIsNotActive() {
        when(userService.isUserActive("user1")).thenReturn(false);
        libraryManager.addBook("book1", 5);

        libraryManager.borrowBook("book1", "user1");

        assertEquals(5, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    void returnBookShouldIncreaseAvailableCopies() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.addBook("book1", 1);
        libraryManager.borrowBook("book1", "user1");

        libraryManager.returnBook("book1", "user1");

        assertEquals(1, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    void returnBookShouldReturnFalseWhenUserDidNotBorrowBook() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.addBook("book1", 1);
        libraryManager.borrowBook("book1", "user1");

        boolean result = libraryManager.returnBook("book1", "user2");

        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({
            "2, false, false, 1.00",
            "2, true, false, 1.50",
            "2, false, true, 0.80",
            "2, true, true, 1.20"
    })
    void calculateDynamicLateFeeShouldReturnCorrectFee(
            int overdueDays,
            boolean isBestseller,
            boolean isPremiumMember,
            double expected
    ) {
        double result = libraryManager.calculateDynamicLateFee(
                overdueDays,
                isBestseller,
                isPremiumMember
        );

        assertEquals(expected, result);
    }

    @Test
    void calculateDynamicLateFeeShouldThrowExceptionWhenOverdueDaysNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> libraryManager.calculateDynamicLateFee(-1, false, false));
    }
}

