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
    fun `test prepareQuery replaces placeholders correctly`() {
        val query =
            "SELECT * FROM table \$FILTER | where GIORNATA_PAGAMENTO between (datetime(\$START_DATE) .. datetime(\$END_DATE))"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val pspId = "PSP123"
        val brokerFiscalCode = "01234556789"

        val result = prepareQuery(query, startDate, endDate, brokerFiscalCode, pspId)

        val expected =
            "SELECT * FROM table | where ID_PSP == \"PSP123\" and ID_BROKER_PSP == \"01234556789\" | where GIORNATA_PAGAMENTO between (datetime(2024-01-01) .. datetime(2024-01-31))"
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

    @Test
    fun `calculateDailyKPIValues returns 0 when percentage is negative`() {
        val result = listOf(-1)
        val totalReports = 100

        val calculatedValue = calculateDailyKPIValues(result, totalReports)

        assertEquals(0, calculatedValue)
    }

    @Test
    fun `calculateDailyKPIValues calculates correct value for normal percentage`() {
        val result = listOf(33)
        val totalReports = 100

        val calculatedValue = calculateDailyKPIValues(result, totalReports)

        assertEquals(33, calculatedValue)
    }

    @Test
    fun `calculateDailyKPIValues rounds correctly for small amounts`() {
        val result = listOf(33)
        val totalReports = 3 // 3 * 33 / 100 = 0.99

        val calculatedValue = calculateDailyKPIValues(result, totalReports)

        assertEquals(1, calculatedValue) // Should round up to 1
    }

    @Test
    fun `calculateDailyKPIValues handles zero totalReports`() {
        val result = listOf(50)
        val totalReports = 0

        val calculatedValue = calculateDailyKPIValues(result, totalReports)

        assertEquals(0, calculatedValue)
    }

    @Test
    fun `calculateDailyKPIValues handles 100 percent`() {
        val result = listOf(100)
        val totalReports = 50

        val calculatedValue = calculateDailyKPIValues(result, totalReports)

        assertEquals(50, calculatedValue)
    }

    @Test
    fun `calculateDailyKPIValues handles Long input`() {
        val result = listOf(50L) // Some databases might return Long instead of Int
        val totalReports = 100

        val calculatedValue = calculateDailyKPIValues(result, totalReports)

        assertEquals(50, calculatedValue)
    }

    @Test
    fun `calculateDailyKPIValues throws exception for empty result list`() {
        val result = emptyList<Any>()
        val totalReports = 100

        assertThrows<IndexOutOfBoundsException> { calculateDailyKPIValues(result, totalReports) }
    }

    @Test
    fun `calculateDailyKPIValues throws exception for invalid type in result`() {
        val result = listOf("not a number")
        val totalReports = 100

        assertThrows<ClassCastException> { calculateDailyKPIValues(result, totalReports) }
    }
}
