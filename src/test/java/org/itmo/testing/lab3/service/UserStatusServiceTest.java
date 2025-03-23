package org.itmo.testing.lab3.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserStatusServiceTest {

    private UserAnalyticsService analyticsMock;
    private UserStatusService statusChecker;

    @BeforeEach
    void prepareMocks() {
        analyticsMock = mock(UserAnalyticsService.class);
        statusChecker = new UserStatusService(analyticsMock);
    }

    @Test
    @DisplayName("Определение последней даты выхода при наличии нескольких сессий")
    void shouldReturnLastSessionDate() {
        UserAnalyticsService.Session firstSession = mock(UserAnalyticsService.Session.class);
        UserAnalyticsService.Session secondSession = mock(UserAnalyticsService.Session.class);

        when(firstSession.getLogoutTime()).thenReturn(LocalDateTime.of(2025, 1, 10, 14, 45));
        when(secondSession.getLogoutTime()).thenReturn(LocalDateTime.of(2025, 5, 20, 19, 30));

        when(analyticsMock.getUserSessions("theta")).thenReturn(List.of(firstSession, secondSession));

        Optional<String> lastSessionDate = statusChecker.getUserLastSessionDate("theta");

        assertAll(
                () -> assertTrue(lastSessionDate.isPresent()),
                () -> assertEquals("2025-05-20", lastSessionDate.get()),
                () -> verify(analyticsMock).getUserSessions("theta")
        );
    }

    @ParameterizedTest
    @CsvSource({
            "alpha, 100, Active",
            "beta, 19, Inactive",
            "gamma, 0, Inactive",
            "delta, 130, Highly active",
            "epsilon, 60, Active",
            "beta, -2, Inactive" //:(...
    })
    @DisplayName("Проверка классификации активности пользователей")
    void shouldDetermineUserStatus(String userId, long minutes, String expectedStatus) {
        when(analyticsMock.getTotalActivityTime(userId)).thenReturn(minutes);

        String actualStatus = statusChecker.getUserStatus(userId);

        assertAll(
                () -> assertEquals(expectedStatus, actualStatus),
                () -> verify(analyticsMock).getTotalActivityTime(userId)
        );
    }

    @Test
    @DisplayName("Обработка случая с одной сессией")
    void shouldWorkWithSingleSession() {
        UserAnalyticsService.Session singleSession = mock(UserAnalyticsService.Session.class);
        when(singleSession.getLogoutTime()).thenReturn(LocalDateTime.of(2025, 7, 14, 16, 0));

        when(analyticsMock.getUserSessions("iota")).thenReturn(List.of(singleSession));

        Optional<String> lastSessionDate = statusChecker.getUserLastSessionDate("iota");

        assertAll(
                () -> assertTrue(lastSessionDate.isPresent()),
                () -> assertEquals("2025-07-14", lastSessionDate.get()),
                () -> verify(analyticsMock).getUserSessions("iota")
        );
    }

    @Test
    @DisplayName("Возвращение пустого результата при отсутствии сессий")
    void shouldReturnEmptyWhenNoSessionsExist() {
        when(analyticsMock.getUserSessions("zeta")).thenReturn(List.of());

        Optional<String> lastSessionDate = statusChecker.getUserLastSessionDate("zeta");

        assertAll(
                () -> assertFalse(lastSessionDate.isPresent()),
                () -> verify(analyticsMock).getUserSessions("zeta")
        );
    }

    @Test
    @DisplayName("Возвращение пустого результата, если getUserSessions возвращает null")
    void shouldHandleNullSessionList() {
        when(analyticsMock.getUserSessions("eta")).thenReturn(null);

        Optional<String> lastSessionDate = statusChecker.getUserLastSessionDate("eta");

        assertAll(
                () -> assertFalse(lastSessionDate.isPresent()),
                () -> verify(analyticsMock).getUserSessions("eta")
        );
    }

    @Test
    @DisplayName("Возвращение пустого результата, если сессии есть, но logoutTime == null")
    void shouldHandleSessionsWithNullLogoutTime() {
        UserAnalyticsService.Session sessionWithoutLogout = mock(UserAnalyticsService.Session.class);

        when(sessionWithoutLogout.getLogoutTime()).thenReturn(null);
        when(analyticsMock.getUserSessions("theta")).thenReturn(List.of(sessionWithoutLogout));

        Optional<String> lastSessionDate = statusChecker.getUserLastSessionDate("theta");

        assertAll(
                () -> assertFalse(lastSessionDate.isPresent()),
                () -> verify(analyticsMock).getUserSessions("theta")
        );
    }
}
