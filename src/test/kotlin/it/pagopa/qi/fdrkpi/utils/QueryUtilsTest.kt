package it.pagopa.qi.fdrkpi.utils

import it.pagopa.qi.fdrkpi.exceptionhandler.DateTooRecentException
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidPeriodException
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QueryUtilsTest {
    @Test
    fun `test preparePspQuery replaces placeholders correctly`() {
        val query =
            "SELECT * FROM table WHERE start_date = '\$START_DATE' AND end_date = '\$END_DATE' AND psp = '\$PSP'"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val psp = "PSP123"

        val result = preparePspQuery(query, startDate, endDate, psp)

        val expected =
            "SELECT * FROM table WHERE start_date = '2024-01-01' AND end_date = '2024-01-31' AND psp = 'PSP123'"
        assertEquals(expected, result)
    }

    @Test
    fun `test getDateRange for daily period`() {
        val date = "2024-01-01"

        val result = getDateRange(FdrKpiPeriod.daily, date)

        val expected = Pair(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1))
        assertEquals(expected, result)
    }

    @Test
    fun `test getDateRange for monthly period`() {
        val date = "2024-01"

        val result = getDateRange(FdrKpiPeriod.monthly, date)

        val expected = Pair(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31))
        assertEquals(expected, result)
    }

    @Test
    fun `test validateDate with valid daily date`() {
        val date = LocalDate.now().minusDays(11).toString()

        assertDoesNotThrow { validateDate("daily", date) }
    }

    @Test
    fun `test validateDate with valid monthly date`() {
        val date = YearMonth.now().minusMonths(1).toString()

        assertDoesNotThrow { validateDate("monthly", date) }
    }

    @Test
    fun `test validateDate throws DateTooRecentException for daily period`() {
        val date = LocalDate.now().minusDays(5).toString() // Too recent

        val exception = assertThrows<DateTooRecentException> { validateDate("daily", date) }
        assertEquals("Date Too Recent", exception.message)
    }

    @Test
    fun `test validateDate throws InvalidPeriodException for invalid period`() {
        val date = "2024-01-01"

        val exception =
            assertThrows<InvalidPeriodException> { validateDate("invalid_period", date) }
        assertEquals("Invalid Period", exception.message)
    }

    @Test
    fun `test validateDate throws DateTooRecentException when daily date is today`() {
        val date = LocalDate.now().toString()

        val exception = assertThrows<DateTooRecentException> { validateDate("daily", date) }
        assertEquals("Date Too Recent", exception.message)
    }
}
